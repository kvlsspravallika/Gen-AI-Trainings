package com.epam.trainings.gen_ai_task_6.service.textEmbedding;

import com.azure.ai.openai.models.EmbeddingItem;
import com.epam.trainings.gen_ai_task_6.model.response.PointResponse;
import io.qdrant.client.grpc.Points;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

public interface TextEmbeddingService {

    String createCollection(String collectionName) throws ExecutionException, InterruptedException;
    List<EmbeddingItem> buildAndGetEmbeddingFromInput(String input);
    String buildAndStoreEmbeddingFromInput(Map<String, Object> input, Optional<String> collectionName) throws ExecutionException, InterruptedException;
    PointResponse getDataFromCollectionWithPointId(String pointId, String collectionName) throws ExecutionException, InterruptedException;
    List<Points.ScoredPoint> search(String input, Optional<String> collectionName) throws ExecutionException, InterruptedException;
}
