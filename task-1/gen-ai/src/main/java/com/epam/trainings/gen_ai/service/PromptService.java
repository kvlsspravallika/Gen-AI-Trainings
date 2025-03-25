package com.epam.trainings.gen_ai.service;

import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromptService {

    private ChatHistory chatHistory;
    private Kernel kernel;
    private ChatCompletionService chatCompletionService;

    public PromptService(ChatHistory chatHistory, Kernel kernel,ChatCompletionService chatCompletionService) {
        this.chatHistory = chatHistory;
        this.kernel = kernel;
        this.chatCompletionService = chatCompletionService;
    }

    public String getPromptResult(String userPrompt) {
        chatHistory.addUserMessage(userPrompt);
        assert chatCompletionService != null;
        List<ChatMessageContent<?>> result =  chatCompletionService.getChatMessageContentsAsync(
                chatHistory,
                kernel,
                null
        ).onErrorMap(ex -> new Exception(ex.getMessage())).block();
        for (ChatMessageContent<?> entry : result) {
            // Print the results
            if (entry.getAuthorRole() == AuthorRole.ASSISTANT && entry.getContent() != null) {
                System.out.println("Assistant > " + result);
            }
            // Add the message from the agent to the chat history
            chatHistory.addMessage(entry);
        }
        return result.toString();
    }
}
