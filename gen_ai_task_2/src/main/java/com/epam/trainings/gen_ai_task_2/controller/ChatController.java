package com.epam.trainings.gen_ai_task_2.controller;

import com.epam.trainings.gen_ai_task_2.request.ChatBotRequest;
import com.epam.trainings.gen_ai_task_2.service.ChatService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gen-ai")
public class ChatController {

    private ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping(value = "/prompt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String promptResponse(@RequestBody ChatBotRequest chatBotRequest) {
        return chatService.getChatResult(chatBotRequest);
    }
}
