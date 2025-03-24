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

    public String getChatResponseAsString(ChatHistory chatHistory) {
        return chatHistory.getMessages().stream()
                .map(chatMessageContent -> chatMessageContent.getAuthorRole().name()
                        + " : " + chatMessageContent.getContent())
                .collect(Collectors.joining("\n\n"));
    }

    public ChatCompletionService getChatCompletionServiceBean(OpenAIAsyncClient openAIAsyncClient,
                                                              String deploymentName) {
        return OpenAIChatCompletion.builder()
                .withModelId(deploymentName)
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build();
    }
}
