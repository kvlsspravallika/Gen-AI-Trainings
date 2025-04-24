package com.epam.trainings.gen_ai_task_6.service.RAG;

import com.epam.trainings.gen_ai_task_6.model.request.ChatBotRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public interface RAGService {

    String uploadAndStoreKnowledge(String url) throws IOException, ExecutionException, InterruptedException ;

    String getResponseFromRAG(ChatBotRequest request) throws ExecutionException, InterruptedException;

    String uploadAndStoreKnowledgeFromFile(MultipartFile file) throws Exception;
}
