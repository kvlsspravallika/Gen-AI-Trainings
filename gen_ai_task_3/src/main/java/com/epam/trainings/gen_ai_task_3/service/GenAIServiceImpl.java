package com.epam.trainings.gen_ai_task_3.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.*;
import com.epam.trainings.gen_ai_task_3.request.ChatBotRequest;
import com.epam.trainings.gen_ai_task_3.util.ResponseGenerator;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ResponseFormat;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class GenAIServiceImpl implements GenAIService{

    private ChatHistory chatHistory;
    private Kernel kernel;
    private ChatCompletionService chatCompletionService;
    private OpenAIAsyncClient openAIAsyncClient;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Value("${azure.openai.deploymentName}")
    private String deploymentName;

    public GenAIServiceImpl(ChatHistory chatHistory, Kernel kernel,
                            ChatCompletionService chatCompletionService,
                            OpenAIAsyncClient openAIAsyncClient) {
        this.chatHistory = chatHistory;
        this.kernel = kernel;
        this.chatCompletionService = chatCompletionService;
        this.openAIAsyncClient = openAIAsyncClient;
    }

    @Override
    public List<String> generateImage(String prompt) throws ExecutionException, InterruptedException {
        ImageGenerationOptions imageGenerationOptions =
                new ImageGenerationOptions(prompt)
                .setN(1)
                .setQuality(ImageGenerationQuality.HD)
                .setSize(ImageSize.SIZE1024X1024);
        System.out.println("Making API request...");
        // for debugging
        ImageGenerations result = openAIAsyncClient.getImageGenerations(deploymentName, imageGenerationOptions)
                .doOnNext(res -> System.out.println("Raw API Response: " + res))
                .doOnError(err -> System.err.println("Error occurred: " + err.getMessage())) // Log error
                .block(); // Blocking call

        System.out.println("Response received: " + result);
        return openAIAsyncClient
                .getImageGenerations(deploymentName, imageGenerationOptions)  // Returns Mono<ImageGenerations>
                .doOnNext(response -> System.out.println("Raw API Response: " + response)) // Debugging
                .map(response -> response.getData().stream()
                        .map(ImageGenerationData::getUrl)  // Extract URLs
                        .collect(Collectors.toList()))  // Convert to List<String>
                .block();
    }

    @Override
    public String getChatResult(ChatBotRequest chatBotRequest) {
        chatHistory.addUserMessage(chatBotRequest.getUserPrompt());
        assert chatCompletionService != null;

        // adding PromptExecutionSetting
        InvocationContext invocationContext = InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withModelId(deploymentName)
                        .withTemperature(chatBotRequest.getTemperature())
                        .withMaxTokens(chatBotRequest.getMaxTokens())
                        .withResponseFormat(ResponseFormat.TEXT)
                        .build())
                .build();
        List<ChatMessageContent<?>> chatResponse =  chatCompletionService.getChatMessageContentsAsync(
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
}
