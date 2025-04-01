package com.epam.trainings.gen_ai_task_3.util;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.ImageGenerations;
import com.epam.trainings.gen_ai_task_3.config.AzureOpenAIProperties;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class ResponseGenerator {

    /**
     * Converts the chat history to a formatted string, where each message is prefixed with
     * the author's role and separated by double newlines.
     *
     * @param chatHistory the chat history object containing the list of chat messages.
     * @return a single string representation of the chat history, where each message is
     *         formatted as "AuthorRole : MessageContent" and separated by double newlines.
     */
    public String getChatResponseAsString(ChatHistory chatHistory) {
        return chatHistory.getMessages().stream()
                .map(chatMessageContent -> chatMessageContent.getAuthorRole().name()
                        + " : " + chatMessageContent.getContent())
                .collect(Collectors.joining("\n\n"));
    }

    /**
     * Creates and returns an instance of {@link ChatCompletionService} configured with the
     * specified OpenAI asynchronous client and deployment model name.
     *
     * @param openAIAsyncClient the OpenAI asynchronous client used for communication with
     *                          the OpenAI API.
     * @param deploymentName the name of the deployment model to be used for generating
     *                        chat completions.
     * @return a configured {@link ChatCompletionService} instance.
     */
    public ChatCompletionService getChatCompletionServiceBean(OpenAIAsyncClient openAIAsyncClient,
                                                              String deploymentName) {
        return OpenAIChatCompletion.builder()
                .withModelId(deploymentName)
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build();
    }
}
