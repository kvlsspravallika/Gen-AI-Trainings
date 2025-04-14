package com.epam.trainings.gen_ai_task_4.util;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.stereotype.Service;

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

}
