package com.epam.trainings.gen_ai_task_5.service;

import com.azure.ai.openai.models.EmbeddingItem;
import com.epam.trainings.gen_ai_task_5.model.response.PointResponse;
import io.qdrant.client.grpc.Points;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public interface TextEmbeddingService {

    String createCollection(String collectionName) throws ExecutionException, InterruptedException;
    List<EmbeddingItem> buildAndGetEmbeddingFromInput(String input);
    String buildAndStoreEmbeddingFromInput(Map<String, Object> input) throws ExecutionException, InterruptedException;
    PointResponse getDataFromCollectionWithPointId(String pointId) throws ExecutionException, InterruptedException;
    List<Points.ScoredPoint> search(String input) throws ExecutionException, InterruptedException;
}
