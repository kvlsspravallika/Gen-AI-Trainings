package com.epam.trainings.gen_ai_task_6;

import com.google.common.util.concurrent.ListenableFuture;
import io.qdrant.client.QdrantClient;
import io.qdrant.client.grpc.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * This creates Qdrant collection on Spring Boot application startup using CommandLineRunner.
 */
@Component
@Slf4j
public class QdrantCollectionCreator implements CommandLineRunner {

    private final QdrantClient qdrantClient;

    @Value("${qdrant.collectionName}")
    private String collectionName;

    public QdrantCollectionCreator(QdrantClient qdrantClient) {
        this.qdrantClient = qdrantClient;
    }

    @Override
    public void run(String... args) throws Exception {// Vector configuration: size = 1536 (like OpenAI embeddings), distance = Cosine
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
        } else {
            log.error("Failed to create collection: {}", collectionName);
        }
    }
}
