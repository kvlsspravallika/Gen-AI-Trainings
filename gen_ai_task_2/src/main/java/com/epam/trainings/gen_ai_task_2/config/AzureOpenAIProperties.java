package com.epam.trainings.gen_ai_task_2.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "azure.openai")
@Getter
@Setter
public class AzureOpenAIProperties {

    private String key;

    private String endpoint;

    private String deploymentName;
}
