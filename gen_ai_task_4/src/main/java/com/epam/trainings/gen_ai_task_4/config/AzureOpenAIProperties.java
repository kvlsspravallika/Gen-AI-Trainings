package com.epam.trainings.gen_ai_task_4.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.openai")
@Getter
@Setter
public class AzureOpenAIProperties {

    private String genericKey;

    private String genericEndPoint;

    private String deploymentName;

    private String imageGenerationKey;

    private String imageGenerationEndpoint;
}
