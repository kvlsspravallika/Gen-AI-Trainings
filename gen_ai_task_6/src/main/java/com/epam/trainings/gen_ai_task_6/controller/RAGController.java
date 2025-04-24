package com.epam.trainings.gen_ai_task_6.controller;

import com.epam.trainings.gen_ai_task_6.model.request.ChatBotRequest;
import com.epam.trainings.gen_ai_task_6.service.RAG.RAGService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/RAG")
public class RAGController {

    private final RAGService RAGService;

    public RAGController(RAGService RAGService) {
        this.RAGService = RAGService;
    }

    @PostMapping(value = "/upload/url/{url}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String uploadKnowledgeFromUrl(@RequestParam String url) throws ExecutionException, InterruptedException, IOException {
        return RAGService.uploadAndStoreKnowledge(url);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public String uploadKnowledgeFromFile(@RequestParam("file") MultipartFile file) throws Exception {
        return RAGService.uploadAndStoreKnowledgeFromFile(file);
    }

    @PostMapping(value = "/ask", produces = MediaType.TEXT_PLAIN_VALUE)
    public String ask(@RequestBody ChatBotRequest request) throws ExecutionException, InterruptedException {
        return RAGService.getResponseFromRAG(request);
    }
}
