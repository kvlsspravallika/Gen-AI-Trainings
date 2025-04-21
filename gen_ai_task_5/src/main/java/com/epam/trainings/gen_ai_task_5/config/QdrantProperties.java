package com.epam.trainings.gen_ai_task_5.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "qdrant")
@Getter
@Setter
public class QdrantProperties {

    private String host;
    private int port;
}

