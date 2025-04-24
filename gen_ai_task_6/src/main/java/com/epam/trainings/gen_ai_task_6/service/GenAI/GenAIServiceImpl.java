package com.epam.trainings.gen_ai_task_6.service.GenAI;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.*;
import com.epam.trainings.gen_ai_task_6.model.DeploymentModel;
import com.epam.trainings.gen_ai_task_6.model.request.ChatBotRequest;
import com.epam.trainings.gen_ai_task_6.model.response.ChatBotResponse;
import com.epam.trainings.gen_ai_task_6.plugins.model.LightsModel;
import com.epam.trainings.gen_ai_task_6.plugins.model.cart.Cart;
import com.epam.trainings.gen_ai_task_6.plugins.model.cart.CartDelta;
import com.epam.trainings.gen_ai_task_6.plugins.model.cart.CheckoutResponse;
import com.epam.trainings.gen_ai_task_6.plugins.model.enums.PizzaSize;
import com.epam.trainings.gen_ai_task_6.plugins.model.enums.PizzaToppings;
import com.epam.trainings.gen_ai_task_6.plugins.model.pizza.Pizza;
import com.epam.trainings.gen_ai_task_6.plugins.model.pizza.PizzaMenu;
import com.epam.trainings.gen_ai_task_6.plugins.model.pizza.RemovePizzaResponse;
import com.epam.trainings.gen_ai_task_6.util.ResponseGenerator;
import com.google.gson.Gson;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypeConverter;
import com.microsoft.semantickernel.contextvariables.ContextVariableTypes;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ResponseFormat;
import com.microsoft.semantickernel.orchestration.ToolCallBehavior;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GenAIServiceImpl implements GenAIService{

    private ChatHistory chatHistory;
    private Kernel kernel;
    private OpenAIAsyncClient openAIAsyncClientForImageGeneration;
    private DeploymentModel deploymentModel;
    private ChatCompletionService chatCompletionService;

    @Autowired
    private ResponseGenerator responseGenerator;

    @Value("${azure.openai.text.deploymentName}")
    private String textDeploymentName;

    @Value(("${azure.openai.image.deploymentName}"))
    private String imageDeploymentName;

    @Value("${prompt.temperature}")
    private Double temperature;

    @Value("${prompt.maxTokens}")
    private Integer maxTokens;

    public GenAIServiceImpl(ChatHistory chatHistory, Kernel kernel,
                            @Qualifier("getOpenAIAsyncClientBeanForImageGeneration") OpenAIAsyncClient openAIAsyncClientForImageGeneration,
                            DeploymentModel deploymentModel,
                            ChatCompletionService chatCompletionService) {
        this.chatHistory = chatHistory;
        this.kernel = kernel;
        this.openAIAsyncClientForImageGeneration = openAIAsyncClientForImageGeneration;
        this.deploymentModel = deploymentModel;
        this.chatCompletionService = chatCompletionService;
    }

    /**
     * Generates a list of image URLs based on the provided text prompt.
     * The method uses an asynchronous AI image generation service to create
     * and retrieve high-quality (HD) images of size 1024x1024 pixels.
     *
     * @param prompt the text input describing the desired content of the image.
     * @return a list of URLs that point to the generated images.
     * @throws ExecutionException if an error occurs during the asynchronous image generation process.
     * @throws InterruptedException if the thread executing the image generation process is interrupted.
     */
    @Override
    public List<String> generateImage(String prompt) throws ExecutionException, InterruptedException {
        ImageGenerationOptions imageGenerationOptions =
                new ImageGenerationOptions(prompt)
                .setN(1)
                .setQuality(ImageGenerationQuality.HD)
                .setSize(ImageSize.SIZE1024X1024);
        ImageGenerations result = openAIAsyncClientForImageGeneration.getImageGenerations(imageDeploymentName, imageGenerationOptions)
                .doOnNext(res -> System.out.println("Raw API Response: " + res))
                .doOnError(err -> System.err.println("Error occurred: " + err.getMessage())) // Log error
                .block(); // Blocking call

        System.out.println("Response received: " + result);
        return openAIAsyncClientForImageGeneration
                .getImageGenerations(imageDeploymentName, imageGenerationOptions)  // Returns Mono<ImageGenerations>
                .doOnNext(response -> System.out.println("Raw API Response: " + response)) // Debugging
                .map(response -> response.getData().stream()
                        .map(ImageGenerationData::getUrl)  // Extract URLs
                        .collect(Collectors.toList()))  // Convert to List<String>
                .block();
    }

    /**
     * Generates a chat result based on the provided ChatBotRequest and an optional AI model name.
     * The method processes the user's prompt, interacts with the AI system to generate a response,
     * and returns the result as a string.
     *
     * @param chatBotRequest the request object containing the user's input prompt, temperature,
     *                       and maximum token count for the response generation.
     * @param modelName an optional parameter specifying the AI model to use for the response
     *                  generation. If not provided, the default deployment model is used.
     * @return the generated chat response as a string.
     */
    @Override
    public String getChatResult(ChatBotRequest chatBotRequest, Optional<String> modelName) {
        chatHistory.addUserMessage(chatBotRequest.getUserPrompt());

        // Add a converter to the kernel to show it how to serialise LightModel objects into a prompt
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(LightsModel.class)
                                .toPromptString(new Gson()::toJson)
                                .build());
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(Pizza.class)
                                .toPromptString(new Gson()::toJson)
                                .build());
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(PizzaMenu.class)
                                .toPromptString(new Gson()::toJson)
                                .build());
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(CartDelta.class)
                                .toPromptString(new Gson()::toJson)
                                .build());
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(PizzaSize.class)
                                .toPromptString(new Gson()::toJson)
                                .build());
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(PizzaToppings.class)
                                .toPromptString(new Gson()::toJson)
                                .build());
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(RemovePizzaResponse.class)
                                .toPromptString(new Gson()::toJson)
                                .build());
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(Cart.class)
                                .toPromptString(new Gson()::toJson)
                                .build());
        ContextVariableTypes
                .addGlobalConverter(
                        ContextVariableTypeConverter.builder(CheckoutResponse.class)
                                .toPromptString(new Gson()::toJson)
                                .build());

        // adding PromptExecutionSetting
        InvocationContext invocationContext = InvocationContext.builder()
                .withPromptExecutionSettings(PromptExecutionSettings.builder()
                        .withModelId(modelName.orElse(textDeploymentName))
                        .withTemperature(chatBotRequest.getTemperature() != null ? chatBotRequest.getTemperature() : temperature)
                        .withMaxTokens(chatBotRequest.getMaxTokens() != null ? chatBotRequest.getMaxTokens() : maxTokens)
                        .withResponseFormat(ResponseFormat.TEXT)
                        .build())
                .withToolCallBehavior(ToolCallBehavior.allowAllKernelFunctions(true))
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


    /**
     * Generates a ChatBotResponse based on the provided model and request details.
     * Depending on the model type, either a text-based chat response or an image URL
     * is generated. If the model type is not recognized, a default response indicating
     * the lack of implementation is returned.
     *
     * @param chatBotRequest the request object containing user input prompt, temperature,
     *                       and token count configurations for the response generation.
     * @param model the specific AI model to use for generating the response. This can
     *              be either a text-based model or an image-based model.
     * @return a ChatBotResponse object containing the generated response or an error message
     *         if the given model type is not supported.
     * @throws ExecutionException if an error occurs during the asynchronous response generation process.
     * @throws InterruptedException if the thread performing the response generation operation is interrupted.
     */
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

    public ChatBotResponse getResponseFromPlugin() {

        return ChatBotResponse.builder()
                .build();
    }
}
