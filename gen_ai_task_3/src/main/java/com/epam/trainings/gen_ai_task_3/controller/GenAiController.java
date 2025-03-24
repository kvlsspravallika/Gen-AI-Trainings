package com.epam.trainings.gen_ai_task_3.controller;

import com.epam.trainings.gen_ai_task_3.request.ChatBotRequest;
import com.epam.trainings.gen_ai_task_3.service.GenAIServiceImpl;
import org.apache.catalina.connector.Response;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/gen-ai")
public class GenAiController {

    private GenAIServiceImpl genAIService;

    public GenAiController(GenAIServiceImpl genAIService) {
        this.genAIService = genAIService;
    }

    @PostMapping(value = "/prompt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String promptResponse(@RequestBody ChatBotRequest chatBotRequest) {
        return genAIService.getChatResult(chatBotRequest);
    }

    @PostMapping(value = "/generateImage/{prompt}")
    public ResponseEntity<List<String>> generateImage(@RequestParam String prompt) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(genAIService.generateImage(prompt));
    }
}
