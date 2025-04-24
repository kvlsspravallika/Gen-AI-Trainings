package com.epam.trainings.gen_ai_task_6.service.GenAI;

import com.epam.trainings.gen_ai_task_6.model.response.ChatBotResponse;
import com.epam.trainings.gen_ai_task_6.model.request.ChatBotRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface GenAIService {

    /**
     * Generates a chat result based on the user-provided prompt and optional AI model.
     * The method processes the input prompt, interacts with the underlying AI chat system,
     * and returns the generated response string.
     *
     * @param chatBotRequest the request object containing the user prompt, temperature, and maxTokens.
     * @param model an optional parameter specifying the AI model to use. If not provided, a default model is used.
     * @return the generated response as a string.
     */
    String getChatResult(ChatBotRequest chatBotRequest, Optional<String> model);

    /**
     * Generates a list of image URLs based on the given prompt by leveraging an AI image generation service.
     * The method requires asynchronous processing and may throw exceptions if execution fails.
     *
     * @param prompt the input text used as a basis for generating the images.
     * @return a list of URLs pointing to the generated images.
     * @throws ExecutionException if an error occurs during the asynchronous image generation process.
     * @throws InterruptedException if the thread executing the image generation task is interrupted.
     */
    List<String> generateImage(String prompt) throws ExecutionException, InterruptedException;

    /**
     * Processes a user's chatbot request and generates a response based on the specified AI model.
     * If the provided model corresponds to a text-based deployment, a textual chatbot response
     * is generated. If the model corresponds to an image-based deployment, an image URL is generated instead.
     * If the provided model is not supported, a default error response is returned.
     *
     * @param chatBotRequest the user's chatbot request containing the input prompt, temperature, and max token count.
     * @param model the name of the AI model to use for generating the response.
     * @return a {@code ChatBotResponse} object containing the generated response, which could be text or an image URL.
     * @throws ExecutionException if an error occurs during the asynchronous response generation process.
     * @throws InterruptedException if the thread executing the response generation task is interrupted.
     */
    ChatBotResponse getResponseBasedOnModel(ChatBotRequest chatBotRequest, String model) throws ExecutionException, InterruptedException;
}
