package com.epam.trainings.gen_ai_task_6.service.textEmbedding;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.models.EmbeddingItem;
import com.azure.ai.openai.models.Embeddings;
import com.azure.ai.openai.models.EmbeddingsOptions;
import com.epam.trainings.gen_ai_task_6.model.response.PointResponse;
import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import io.qdrant.client.grpc.JsonWithInt;
import io.qdrant.client.grpc.Points;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
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

    @Value("${azure.openai.embedding.deploymentName}")
    private String deploymentName;

    @Value("${qdrant.collectionName}")
    private String commonCollectionName;


    public TextEmbeddingServiceImpl(QdrantClient qdrantClient,
                                    @Qualifier("getGenericOpenAIAsyncClientBean") OpenAIAsyncClient openAIAsyncClient) {
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
    public String buildAndStoreEmbeddingFromInput(Map<String, Object> input, Optional<String> collectionName) throws ExecutionException, InterruptedException {
        String genericSummarizedString = getGenericSummarizedString(input);
        List<EmbeddingItem> embeddingItemsList = buildAndGetEmbeddingFromInput(genericSummarizedString);
        var points = new ArrayList<List<Float>>();
        embeddingItemsList.forEach(
                embeddingItem ->
                        points.add(new ArrayList<>(embeddingItem.getEmbedding())));
        var pointStructs = new ArrayList<Points.PointStruct>();
        points.forEach(point ->
                pointStructs.add(getPointStruct(point, input)));
        return saveVector(pointStructs, collectionName.isPresent() ? collectionName : Optional.of(commonCollectionName));
    }

    private String getGenericSummarizedString(Map<String, Object> input) {
        StringBuilder summary = new StringBuilder();

        // Sort keys to ensure consistent order
        input.keySet().stream().sorted().forEach(key -> {
            Object value = input.get(key);
            String displayValue;

            if (value instanceof Boolean) {
                displayValue = (Boolean) value ? "Yes" : "No";
            } else if (value instanceof Number) {
                displayValue = String.format("%.2f", ((Number) value).doubleValue());
            } else {
                displayValue = value.toString().trim().toLowerCase();
            }

            // Use "key=value" format for clarity and consistency
            summary.append(key.toLowerCase()).append("=").append(displayValue).append("; ");
        });

        String result = summary.toString().trim();
        log.info("Standardized summary string: {}", result);
        return result;
    }

    /**
     * Constructs a point structure from a list of float values and metadata extracted from any object.
     *
     * @param point the vector values
     * @param input the input object (can be of any type)
     * @return a {@link Points.PointStruct} containing the vector and payload from input object fields
     */
    private Points.PointStruct getPointStruct(List<Float> point, Map<String, Object> input) {
        Map<String, JsonWithInt.Value> payloadMap = new HashMap<>();
        for (Map.Entry<String, Object> entry : input.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            payloadMap.put(key, toValue(value));
            System.out.println(key + ": " + value);
        }

        Points.PointStruct pointStruct = Points.PointStruct.newBuilder()
                .setId(id(UUID.randomUUID()))
                .setVectors(vectors(point))
                .putAllPayload(payloadMap)
                .build();
        log.info("Point struct: {}", pointStruct.getPayloadMap());
        return pointStruct;
    }

    /**
     * Converts various data types to {@link JsonWithInt.Value}.
     *
     * @param obj the value to convert
     * @return the corresponding JsonWithInt.Value
     */
    private JsonWithInt.Value toValue(Object obj) {
        JsonWithInt.Value.Builder builder = JsonWithInt.Value.newBuilder();
        if (obj instanceof String str) {
            builder.setStringValue(str);
        } else if (obj instanceof Integer i) {
            builder.setIntegerValue(i);
        } else if (obj instanceof Double d) {
            builder.setDoubleValue(d);
        } else if (obj instanceof Float f) {
            builder.setDoubleValue(f.doubleValue());
        } else if (obj instanceof Boolean b) {
            builder.setBoolValue(b);
        } else if (obj instanceof Long l) {
            builder.setIntegerValue(l.intValue()); // or use setLongValue if available
        } else {
            builder.setStringValue(obj.toString()); // fallback
        }
        return builder.build();
    }

    private List<EmbeddingItem> getEmbeddingItems(Mono<Embeddings> embeddings){
        log.info("fetching data from embeddings.........");
        return embeddings.block().getData();
    }

    public PointResponse getDataFromCollectionWithPointId(String pointId, String collectionName) throws ExecutionException, InterruptedException {
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
    public List<Points.ScoredPoint> search(String input, Optional<String> collectionName) throws ExecutionException, InterruptedException {
        var embeddingsOptions = new EmbeddingsOptions(List.of(input));
        var embeddings = openAIAsyncClient.getEmbeddings(deploymentName, embeddingsOptions);
        log.info("fetched embeddings from OpenAI client..........");
        log.info("checking if embeddings are null......{}", Objects.requireNonNull(embeddings.block()));
        var inputEmbeddings = new ArrayList<Float>();
        getEmbeddingItems(embeddings).forEach(embeddingItem ->
                inputEmbeddings.addAll(embeddingItem.getEmbedding())
        );
        List<Points.ScoredPoint> result = qdrantClient.searchAsync(Points.SearchPoints.newBuilder()
                .setCollectionName(collectionName.isPresent() ? collectionName.orElse("") : commonCollectionName)
                .addAllVector(inputEmbeddings)
                        .setLimit(10)
                .setWithPayload(enable(true))
                .build()).get();
        log.info("completed fetching search result.......");
        return result;
    }

    /**
     * Saves the list of point structures (vectors) to the Qdrant collection.
     *
     * @param pointStructs the list of vectors to be saved
     * @throws InterruptedException if the thread is interrupted during execution
     * @throws ExecutionException if the saving operation fails
     */
    public String saveVector(ArrayList<Points.PointStruct> pointStructs, Optional<String> collectionName) throws InterruptedException, ExecutionException {
        Points.UpdateResult response = qdrantClient.upsertAsync(collectionName.isPresent() ? String.valueOf(collectionName) : commonCollectionName, pointStructs).get();
        if (response!=null && response.getStatus() == Points.UpdateStatus.Completed) {
            log.info("Upsert successful! Operation ID: {}  ", response.getOperationId());
            return "Vector successfully stored successfully with Point id : " + pointStructs.getFirst().getId();
        } else {
            log.error("Upsert failed with status:  {} ", (response != null ? response.getStatus() : "null"));
            return "Failed to store vector";
        }
    }

}
