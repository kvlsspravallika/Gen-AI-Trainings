package com.epam.trainings.gen_ai_task_3.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "deployment.embedding")
@Getter
@Setter
public class TextEmbeddingModelConfig {

    private List<String> models;
}
