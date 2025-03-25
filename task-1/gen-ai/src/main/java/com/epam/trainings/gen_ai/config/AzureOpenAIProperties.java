package com.epam.trainings.gen_ai.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "azure.openai")
@Getter
@Setter
public class AzureOpenAIProperties {

    private String key;

    private String endpoint;

    private String deploymentName;
}
