package com.epam.trainings.gen_ai_task_3.controller;

import com.epam.trainings.gen_ai_task_3.model.ChatBotResponse;
import com.epam.trainings.gen_ai_task_3.model.request.ChatBotRequest;
import com.epam.trainings.gen_ai_task_3.service.GenAIServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/gen-ai")
public class GenAiController {

    private GenAIServiceImpl genAIService;

    public GenAiController(GenAIServiceImpl genAIService) {
        this.genAIService = genAIService;
    }

    /**
     * Handles POST requests to the "/prompt" endpoint and generates a textual response
     * based on the provided chatbot request data.
     *
     * @param chatBotRequest the request payload containing user input, temperature,
     *                       and maximum token count for the generated response.
     * @return a textual response generated by the AI service based on the provided input.
     */
    @PostMapping(value = "/prompt", produces = MediaType.TEXT_PLAIN_VALUE)
    public String promptResponse(@RequestBody ChatBotRequest chatBotRequest) {
        return genAIService.getChatResult(chatBotRequest, Optional.empty());
    }

    /**
     * Generates an image based on the provided prompt using the AI service and returns a list of image URLs.
     *
     * @param prompt the user-provided text prompt describing the image to be generated.
     * @return a ResponseEntity containing a list of URLs pointing to the generated images.
     * @throws ExecutionException if an exception occurs during the execution of the image generation process.
     * @throws InterruptedException if the thread executing the image generation process is interrupted.
     */
    @PostMapping(value = "/generateImage/{prompt}")
    public ResponseEntity<List<String>> generateImage(@RequestParam String prompt) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(genAIService.generateImage(prompt));
    }

    /**
     * Generates a response based on the specified AI model and user's request payload.
     *
     * @param model the name of the AI model to be used for generating the response.
     * @param chatBotRequest the user's request containing relevant input data such as the prompt, temperature,
     *                       and maximum token count for generating the response.
     * @return a ResponseEntity containing the generated response as an instance of {@code ChatBotResponse}.
     * @throws ExecutionException if an error occurs during the response generation process.
     * @throws InterruptedException if the response generation process is interrupted.
     */
    @PostMapping(value = "/generate/{model}")
    public ResponseEntity<ChatBotResponse> generateResponseBasedOnAIModelPassed(@RequestParam String model, @RequestBody ChatBotRequest chatBotRequest) throws ExecutionException, InterruptedException {
        return ResponseEntity.ok(genAIService.getResponseBasedOnModel(chatBotRequest, model));
    }
}
