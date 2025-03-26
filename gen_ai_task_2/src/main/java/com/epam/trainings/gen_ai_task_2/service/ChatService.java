package com.epam.trainings.gen_ai_task_2.service;

import com.epam.trainings.gen_ai_task_2.request.ChatBotRequest;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.orchestration.PromptExecutionSettings;
import com.microsoft.semantickernel.orchestration.ResponseFormat;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    private ChatHistory chatHistory;
    private Kernel kernel;
    private ChatCompletionService chatCompletionService;

    @Value("${azure.openai.deploymentName}")
    private String deploymentName;

    public ChatService(ChatHistory chatHistory, Kernel kernel, ChatCompletionService chatCompletionService) {
        this.chatHistory = chatHistory;
        this.kernel = kernel;
        this.chatCompletionService = chatCompletionService;
    }

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

        return getChatResponseAsString(chatHistory);
    }

    private String getChatResponseAsString(ChatHistory chatHistory) {
        return chatHistory.getMessages().stream()
                .map(chatMessageContent -> chatMessageContent.getAuthorRole().name()
                        + " : " + chatMessageContent.getContent())
                .collect(Collectors.joining("\n\n"));
    }
}
