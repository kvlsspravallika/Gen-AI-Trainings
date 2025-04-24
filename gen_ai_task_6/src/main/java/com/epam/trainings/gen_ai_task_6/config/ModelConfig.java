package com.epam.trainings.gen_ai_task_6.config;

import com.epam.trainings.gen_ai_task_6.model.DeploymentModel;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({TextModelConfig.class, ImageModelConfig.class, TextEmbeddingModelConfig.class })
public class ModelConfig {

    @Bean
    public DeploymentModel getDeploymentModel(TextModelConfig textModelConfig, ImageModelConfig imageModelConfig,
                                              TextEmbeddingModelConfig textEmbeddingModelConfig) {
        return DeploymentModel.builder()
                .textModels(textModelConfig.getModels())
                .imageModels(imageModelConfig.getModels())
                .textEmbeddingModels(textEmbeddingModelConfig.getModels())
                .build();
    }
}
