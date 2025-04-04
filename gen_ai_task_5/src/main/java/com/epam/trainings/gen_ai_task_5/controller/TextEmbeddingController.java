package com.epam.trainings.gen_ai_task_5.controller;

import com.azure.ai.openai.models.EmbeddingItem;
import com.epam.trainings.gen_ai_task_5.model.FoodItem;
import com.epam.trainings.gen_ai_task_5.service.TextEmbeddingServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/text-embedding")
public class TextEmbeddingController {

    private final TextEmbeddingServiceImpl textEmbeddingServiceImpl;

    public TextEmbeddingController(TextEmbeddingServiceImpl textEmbeddingServiceImpl) {
        this.textEmbeddingServiceImpl = textEmbeddingServiceImpl;
    }

    @PostMapping(value = "/create/{collectionName}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String createCollection(@RequestParam String collectionName) throws ExecutionException, InterruptedException {
        return textEmbeddingServiceImpl.createCollection(collectionName);
    }

    @PostMapping(value = "/buildEmbedding/{input}", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<EmbeddingItem> buildEmbedding(@RequestParam String input) {
        return textEmbeddingServiceImpl.buildAndGetEmbeddingFromInput(input);
    }

    @PostMapping(value = "/buildAndStoreEmbedding/collection/{collectionName}", produces = MediaType.TEXT_PLAIN_VALUE)
    public String buildAndStoreEmbedding(@RequestBody FoodItem input, @RequestParam String collectionName) throws ExecutionException, InterruptedException {
        return textEmbeddingServiceImpl.buildAndStoreEmbeddingFromInput(input, collectionName);
    }

    @GetMapping(value = "/collection/{collectionName}/point/{pointId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getDataFromCollectionWithPointId(@RequestParam String collectionName, @RequestParam String pointId) throws ExecutionException, InterruptedException {
        return textEmbeddingServiceImpl.getDataFromCollectionWithPointId(collectionName, pointId).toString();
    }
}
