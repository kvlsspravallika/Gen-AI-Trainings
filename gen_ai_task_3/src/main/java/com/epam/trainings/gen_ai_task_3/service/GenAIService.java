package com.epam.trainings.gen_ai_task_3.service;

import com.epam.trainings.gen_ai_task_3.request.ChatBotRequest;

import java.util.List;
import java.util.concurrent.ExecutionException;

public interface GenAIService {

    String getChatResult(ChatBotRequest chatBotRequest);

    List<String> generateImage(String prompt) throws ExecutionException, InterruptedException;
}
