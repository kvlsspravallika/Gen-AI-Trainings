package com.epam.trainings.gen_ai_task_5.util;

import com.azure.ai.openai.models.EmbeddingItem;
import com.epam.trainings.gen_ai_task_5.model.FoodItem;
import com.epam.trainings.gen_ai_task_5.service.TextEmbeddingServiceImpl;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.VectorsFactory.vectors;

@Slf4j
public class FoodItemEmbeddingHelper {

    private final TextEmbeddingServiceImpl textEmbeddingService;
    public FoodItemEmbeddingHelper(TextEmbeddingServiceImpl textEmbeddingService) {
        this.textEmbeddingService = textEmbeddingService;
    }

    private String storeEmbedding(FoodItem input) throws ExecutionException, InterruptedException {
        String summarizedString = getSummarizedTextStringForFoodItem(input);
        List<EmbeddingItem> embeddingItemsList = textEmbeddingService.buildAndGetEmbeddingFromInput(summarizedString);
        var points = new ArrayList<List<Float>>();
        embeddingItemsList.forEach(
                embeddingItem ->
                        points.add(new ArrayList<>(embeddingItem.getEmbedding())));
        var pointStructs = new ArrayList<Points.PointStruct>();
        points.forEach(point ->
                pointStructs.add(getPointStruct(point, input)));
        return textEmbeddingService.saveVector(pointStructs);
    }

    private String getSummarizedTextStringForFoodItem(FoodItem item) {
        String summarizedString =  String.format(
                "Name: %s\nDescription: %s\nPrice: $%.2f\nCuisine: %s\nVegetarian: %s\nRating: %f",
                item.getName(),
                item.getDescription(),
                item.getPrice(),
                item.getCuisine(),
                item.isVegetarian() ? "Yes" : "No",
                item.getRating()
        );
        log.info("Summarized String:   {}", summarizedString);
        return summarizedString;
    }

    /**
     * Constructs a point structure from a list of float values representing a vector.
     *
     * @param point the vector values
     * @return a {@link Points.PointStruct} object containing the vector and associated metadata
     */

    private Points.PointStruct getPointStruct(List<Float> point, FoodItem input) {
        HashMap<String, JsonWithInt.Value> payloadMap = new HashMap<>();
        payloadMap.put("name", JsonWithInt.Value.newBuilder().setStringValue(input.getName()).build());
        payloadMap.put("description", JsonWithInt.Value.newBuilder().setStringValue(input.getDescription()).build());
        payloadMap.put("cuisine", JsonWithInt.Value.newBuilder().setStringValue(input.getCuisine()).build());
        payloadMap.put("rating", JsonWithInt.Value.newBuilder().setDoubleValue(input.getRating()).build());
        payloadMap.put("price", JsonWithInt.Value.newBuilder().setDoubleValue(input.getPrice()).build());
        payloadMap.put("isVegetarian", JsonWithInt.Value.newBuilder().setBoolValue(input.isVegetarian()).build());
        return Points.PointStruct.newBuilder()
                .setId(id(UUID.randomUUID()))
                .setVectors(vectors(point))
                .putAllPayload(payloadMap)
                .build();
    }
}
