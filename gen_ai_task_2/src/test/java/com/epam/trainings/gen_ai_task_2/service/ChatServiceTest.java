package com.epam.trainings.gen_ai_task_2.service;

import com.epam.trainings.gen_ai_task_2.request.ChatBotRequest;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.orchestration.InvocationContext;
import com.microsoft.semantickernel.services.chatcompletion.AuthorRole;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import com.microsoft.semantickernel.services.chatcompletion.ChatMessageContent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatHistory chatHistory;

    @Mock
    private Kernel kernel;

    @Mock
    private ChatCompletionService chatCompletionService;

    @InjectMocks
    private ChatService chatService;

    @Value("${azure.openai.deploymentName}")
    private String deploymentName;

    @BeforeEach
    void setUp() {
        deploymentName = "test-deployment";
    }

    @Test
    void testGetPromptResult() {
        ChatBotRequest chatBotRequest = new ChatBotRequest("Hello", 0.7, 100);
        List<ChatMessageContent<?>> mockResponse = List.of(new ChatMessageContent<>(AuthorRole.ASSISTANT, "Hello, how can I help you?"));

        when(chatCompletionService.getChatMessageContentsAsync(any(ChatHistory.class), any(Kernel.class), any(InvocationContext.class)))
                .thenReturn(Mono.just(mockResponse));

        String response = chatService.getPromptResult(chatBotRequest);

        assertNotNull(response);

        verify(chatHistory).addUserMessage("Hello");
        verify(chatHistory).addSystemMessage("Hello, how can I help you?");
    }

    @Test
    void testGetPromptResultWithEmptyUserInput() {
        ChatBotRequest chatBotRequest = new ChatBotRequest("", 0.7, 100);
        List<ChatMessageContent<?>> mockResponse = List.of(new ChatMessageContent<>(AuthorRole.ASSISTANT, "Can you please clarify?"));

        when(chatCompletionService.getChatMessageContentsAsync(any(ChatHistory.class), any(Kernel.class), any(InvocationContext.class)))
                .thenReturn(Mono.just(mockResponse));

        String response = chatService.getPromptResult(chatBotRequest);

        assertNotNull(response);

        verify(chatHistory).addUserMessage("");
        verify(chatHistory).addSystemMessage("Can you please clarify?");
    }

}
