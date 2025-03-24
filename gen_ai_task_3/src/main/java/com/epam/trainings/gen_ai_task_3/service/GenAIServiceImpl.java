package com.epam.trainings.gen_ai_task_3.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.*;
import com.epam.trainings.gen_ai_task_3.model.ChatBotResponse;
import com.epam.trainings.gen_ai_task_3.model.DeploymentModel;
import com.epam.trainings.gen_ai_task_3.model.request.ChatBotRequest;
import com.epam.trainings.gen_ai_task_3.util.ResponseGenerator;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ResponseFormat;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class GenAIServiceImpl implements GenAIService{

    private ChatHistory chatHistory;
    private Kernel kernel;
    private OpenAIAsyncClient genericOpenAiAsyncClient;
    private OpenAIAsyncClient openAIAsyncClientForImageGeneration;
    private DeploymentModel deploymentModel;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Value("${azure.openai.deploymentName}")
    private String deploymentName;

    @Value("${prompt.temperature}")
    private Double temperature;

    @Value("${prompt.maxTokens}")
    private Integer maxTokens;

    public GenAIServiceImpl(ChatHistory chatHistory, Kernel kernel,
                            @Qualifier("getGenericOpenAIAsyncClientBean") OpenAIAsyncClient genericOpenAiAsyncClient,
                            @Qualifier("getOpenAIAsyncClientBeanForImageGeneration") OpenAIAsyncClient openAIAsyncClientForImageGeneration,
                            DeploymentModel deploymentModel) {
        this.chatHistory = chatHistory;
        this.kernel = kernel;
        this.genericOpenAiAsyncClient = genericOpenAiAsyncClient;
        this.openAIAsyncClientForImageGeneration = openAIAsyncClientForImageGeneration;
        this.deploymentModel = deploymentModel;
    }

    @Override
    public List<String> generateImage(String prompt) throws ExecutionException, InterruptedException {
        ImageGenerationOptions imageGenerationOptions =
                new ImageGenerationOptions(prompt)
                .setN(1)
                .setQuality(ImageGenerationQuality.HD)
                .setSize(ImageSize.SIZE1024X1024);
        ImageGenerations result = openAIAsyncClientForImageGeneration.getImageGenerations(deploymentName, imageGenerationOptions)
                .doOnNext(res -> System.out.println("Raw API Response: " + res))
                .doOnError(err -> System.err.println("Error occurred: " + err.getMessage())) // Log error
                .block(); // Blocking call

        System.out.println("Response received: " + result);
        return openAIAsyncClientForImageGeneration
                .getImageGenerations(deploymentName, imageGenerationOptions)  // Returns Mono<ImageGenerations>
                .doOnNext(response -> System.out.println("Raw API Response: " + response)) // Debugging
                .map(response -> response.getData().stream()
                        .map(ImageGenerationData::getUrl)  // Extract URLs
                        .collect(Collectors.toList()))  // Convert to List<String>
                .block();
    }

    @Override
    public String getChatResult(ChatBotRequest chatBotRequest, Optional<String> modelName) {
        chatHistory.addUserMessage(chatBotRequest.getUserPrompt());

        // adding PromptExecutionSetting
        InvocationContext invocationContext = InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withModelId(modelName.orElse(deploymentName))
                        .withTemperature(chatBotRequest.getTemperature() != null ? chatBotRequest.getTemperature() : temperature)
                        .withMaxTokens(chatBotRequest.getMaxTokens() != null ? chatBotRequest.getMaxTokens() : maxTokens)
                        .withResponseFormat(ResponseFormat.TEXT)
                        .build())
                .build();
        List<ChatMessageContent<?>> chatResponse =  responseGenerator
                .getChatCompletionServiceBean(genericOpenAiAsyncClient, modelName.orElse(deploymentName)).getChatMessageContentsAsync(
                chatHistory,
                kernel,
                invocationContext
        ).onErrorMap(ex -> new Exception(ex.getMessage())).block();

        assert chatResponse != null;
        chatHistory.addSystemMessage(chatResponse.stream()
                .map(ChatMessageContent::getContent)
                .collect(Collectors.joining(" ")));

        return responseGenerator.getChatResponseAsString(chatHistory);
    }


    @Override
    public ChatBotResponse getResponseBasedOnModel(ChatBotRequest chatBotRequest, String model) throws ExecutionException, InterruptedException {
        if(deploymentModel.getTextModels().contains(model)) {
            return ChatBotResponse.builder()
                    .response(getChatResult(chatBotRequest, Optional.of(model))).build();
        } else if(deploymentModel.getImageModels().contains(model)) {
            return ChatBotResponse.builder()
                    .response(generateImage(chatBotRequest.getUserPrompt()).get(0)).build();
        }
        return ChatBotResponse.builder()
                .response("No such AI deployment model implemented").build();
    }
}
