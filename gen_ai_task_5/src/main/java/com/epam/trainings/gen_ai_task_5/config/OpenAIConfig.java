package com.epam.trainings.gen_ai_task_5.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AzureOpenAIProperties.class)
public class OpenAIConfig {

    /**
     * Provides a Spring bean for an instance of OpenAIAsyncClient configured for generic purposes.
     * The client is built using the credentials and endpoint information provided in the AzureOpenAIProperties.
     *
     * @param azureOpenAIProperties the Azure-specific configuration properties containing the
     *                               API key and endpoint for the OpenAI Async client.
     * @return an instance of OpenAIAsyncClient configured with the provided credentials and endpoint.
     */
    @Bean
    public OpenAIAsyncClient getGenericOpenAIAsyncClientBean(AzureOpenAIProperties azureOpenAIProperties) {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(azureOpenAIProperties.getGenericKey()))
                .endpoint(azureOpenAIProperties.getGenericEndPoint())
                .buildAsyncClient();
    }
}
