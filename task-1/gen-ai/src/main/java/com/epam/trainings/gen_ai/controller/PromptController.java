package com.epam.trainings.gen_ai.controller;

import com.epam.trainings.gen_ai.service.PromptService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gen-ai")
public class PromptController {

    private PromptService promptService;

    public PromptController(PromptService promptService) {
        this.promptService = promptService;
    }

    @GetMapping("/prompt")
    public String promptResponse(@RequestParam("userPrompt") String userPrompt) {
        return promptService.getPromptResult(userPrompt);
    }
}
