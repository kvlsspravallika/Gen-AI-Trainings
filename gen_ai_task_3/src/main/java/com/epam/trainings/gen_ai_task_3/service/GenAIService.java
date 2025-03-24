package com.epam.trainings.gen_ai_task_3.service;

import com.epam.trainings.gen_ai_task_3.model.ChatBotResponse;
import com.epam.trainings.gen_ai_task_3.model.request.ChatBotRequest;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface GenAIService {

    String getChatResult(ChatBotRequest chatBotRequest, Optional<String> model);

    List<String> generateImage(String prompt) throws ExecutionException, InterruptedException;

    ChatBotResponse getResponseBasedOnModel(ChatBotRequest chatBotRequest, String model) throws ExecutionException, InterruptedException;
}
