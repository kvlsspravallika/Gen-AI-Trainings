package com.epam.trainings.gen_ai_task_6.config.qdrant;

import io.qdrant.client.QdrantClient;
import io.qdrant.client.QdrantGrpcClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutionException;

/**
 * Configuration class for setting up the Qdrant Client.
 * <p>
 * This configuration defines a bean that provides a client for interacting
 * with a Qdrant service. The client is built using gRPC to connect to a
 * Qdrant instance running at the specified host and port.
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(QdrantProperties.class)
public class QdrantConfiguration {

    /**
     * Creates a {@link QdrantClient} bean for interacting with the Qdrant service.
     *
     * @return an instance of {@link QdrantClient}
     */
    @Bean
    public QdrantClient qdrantClient(QdrantProperties qdrantProperties) throws ExecutionException, InterruptedException {
        QdrantClient client = new QdrantClient(QdrantGrpcClient
                .newBuilder(qdrantProperties.getHost(), qdrantProperties.getPort(), false).build());
        return client;
    }

}
