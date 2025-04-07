package com.epam.trainings.gen_ai_task_5.service;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.epam.trainings.gen_ai_task_5.model.FoodItem;
import com.epam.trainings.gen_ai_task_5.model.response.PointResponse;
import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.concurrent.ExecutionException;

import static io.qdrant.client.PointIdFactory.id;
import static io.qdrant.client.VectorsFactory.vectors;
import static io.qdrant.client.WithPayloadSelectorFactory.enable;

@Slf4j
@Component
public class TextEmbeddingServiceImpl implements  TextEmbeddingService{

    private final QdrantClient qdrantClient;
    private final OpenAIAsyncClient openAIAsyncClient;

    @Value("${azure.openai.deploymentName}")
    private String deploymentName;


    public TextEmbeddingServiceImpl(QdrantClient qdrantClient, OpenAIAsyncClient openAIAsyncClient) {
        this.qdrantClient = qdrantClient;
        this.openAIAsyncClient = openAIAsyncClient;
    }

    @Override
    public String createCollection(String collectionName) throws ExecutionException, InterruptedException {
        // Vector configuration: size = 1536 (like OpenAI embeddings), distance = Cosine
        Collections.VectorParams vectorParams = Collections.VectorParams.newBuilder()
                .setSize(1536)
                .setDistance(Collections.Distance.Cosine)
                .build();

        // Create the collection request
        Collections.CreateCollection createRequest = Collections
                .CreateCollection.newBuilder()
                .setCollectionName(collectionName)
                .setVectorsConfig(
                        Collections.VectorsConfig.newBuilder()
                        .setParams(vectorParams).build())
                .build();

        ListenableFuture<Collections.CollectionOperationResponse> future = qdrantClient.createCollectionAsync(createRequest);
        // Handle the response
        Collections.CollectionOperationResponse response = future.get(); // blocks until result

        if (response.getResult()) {
            log.info("Collection {} created successfully....", collectionName);
            return String.format("Collection created successfully %s", collectionName);
        } else {
            log.error("Failed to create collection: {}", collectionName);
            return String.format("Error creating collection %s", collectionName);
        }
    }

    @Override
    public List<EmbeddingItem> buildAndGetEmbeddingFromInput(String input) {
        var embeddingsOptions = new EmbeddingsOptions(List.of(input));
        var embeddings = openAIAsyncClient.getEmbeddings(deploymentName, embeddingsOptions);
        log.info("fetched embeddings from OpenAI client..........");
        log.info("checking if embeddings are null......{}", Objects.requireNonNull(embeddings.block()));
        return getEmbeddingItems(embeddings);
    }

    @Override
    public String buildAndStoreEmbeddingFromInput(FoodItem input, String collectionName) throws ExecutionException, InterruptedException {
        return storeEmbedding(input, collectionName);
    }

    private List<EmbeddingItem> getEmbeddingItems(Mono<Embeddings> embeddings){
        log.info("fetching data from embeddings.........");
        return embeddings.block().getData();
    }

    public PointResponse getDataFromCollectionWithPointId(String collectionName, String pointId) throws ExecutionException, InterruptedException {
        ListenableFuture<List<Points.RetrievedPoint>>  result = qdrantClient.retrieveAsync(
                collectionName,
                List.of(Points.PointId.newBuilder().setUuid(pointId).build()),
                true,
                true, Points.ReadConsistency.newBuilder()
                        .setType(Points.ReadConsistencyType.Majority).build()
        );

        PointResponse pointResponse = new PointResponse();
        // Block and get result
        List<Points.RetrievedPoint> retrievedPoints = result.get();

        for (Points.RetrievedPoint point : retrievedPoints) {
            pointResponse.setPointId(point.getId().getUuid());
            pointResponse.setPayloadMap(point.getPayloadMap());
            pointResponse.setVectors(point.getVectors().getVector().getDataList());
        }
        return pointResponse;
    }

    @Override
    public List<Points.ScoredPoint> search(String input) throws ExecutionException, InterruptedException {
        var embeddingsOptions = new EmbeddingsOptions(List.of(input));
        var embeddings = openAIAsyncClient.getEmbeddings(deploymentName, embeddingsOptions);
        log.info("fetched embeddings from OpenAI client..........");
        log.info("checking if embeddings are null......{}", Objects.requireNonNull(embeddings.block()));
        var inputEmbeddings = new ArrayList<Float>();
        getEmbeddingItems(embeddings).forEach(embeddingItem ->
                inputEmbeddings.addAll(embeddingItem.getEmbedding())
        );
        List<Points.ScoredPoint> result = qdrantClient.searchAsync(Points.SearchPoints.newBuilder()
                .setCollectionName("food-items")
                .addAllVector(inputEmbeddings)
                        .setLimit(10)
                .setWithPayload(enable(true))
                .build()).get();
        log.info("completed fetching search result.......");
        return result;
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

    private String storeEmbedding(FoodItem input, String collectionName) throws ExecutionException, InterruptedException {
        String summarizedString = getSummarizedTextStringForFoodItem(input);
        List<EmbeddingItem> embeddingItemsList = buildAndGetEmbeddingFromInput(summarizedString);
        var points = new ArrayList<List<Float>>();
        embeddingItemsList.forEach(
                embeddingItem ->
                        points.add(new ArrayList<>(embeddingItem.getEmbedding())));
        var pointStructs = new ArrayList<Points.PointStruct>();
        points.forEach(point ->
                pointStructs.add(getPointStruct(point, input)));
        return saveVector(pointStructs, collectionName);
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

    /**
     * Saves the list of point structures (vectors) to the Qdrant collection.
     *
     * @param pointStructs the list of vectors to be saved
     * @throws InterruptedException if the thread is interrupted during execution
     * @throws ExecutionException if the saving operation fails
     */
    private String saveVector(ArrayList<Points.PointStruct> pointStructs, String collectionName) throws InterruptedException, ExecutionException {
        Points.UpdateResult response = qdrantClient.upsertAsync(collectionName, pointStructs).get();
        if (response!=null && response.getStatus() == Points.UpdateStatus.Completed) {
            log.info("Upsert successful! Operation ID: {}  ", response.getOperationId());
            return "Vector successfully stored successfully with Point id : " + pointStructs.getFirst().getId();
        } else {
            log.error("Upsert failed with status:  {} ", (response != null ? response.getStatus() : "null"));
            return "Failed to store vector";
        }
    }

}
