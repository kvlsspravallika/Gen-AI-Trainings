package com.epam.trainings.gen_ai_task_3.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AzureOpenAIProperties.class)
public class SemanticKernelConfig {

    @Bean(name = "getGenericOpenAIAsyncClientBean")
    public OpenAIAsyncClient getGenericOpenAIAsyncClientBean(AzureOpenAIProperties azureOpenAIProperties) {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(azureOpenAIProperties.getGenericKey()))
                .endpoint(azureOpenAIProperties.getGenericEndPoint())
                .buildAsyncClient();
    }

    @Bean(name = "getOpenAIAsyncClientBeanForImageGeneration")
    public OpenAIAsyncClient getOpenAIAsyncClientBeanForImageGeneration(AzureOpenAIProperties azureOpenAIProperties) {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(azureOpenAIProperties.getImageGenerationKey()))
                .endpoint(azureOpenAIProperties.getImageGenerationEndpoint())
                .buildAsyncClient();
    }

    @Bean
    public ChatCompletionService getChatCompletionServiceBean(@Qualifier("getGenericOpenAIAsyncClientBean") OpenAIAsyncClient openAIAsyncClient,
                                                              AzureOpenAIProperties azureOpenAIProperties) {
        return OpenAIChatCompletion.builder()
                .withModelId(azureOpenAIProperties.getDeploymentName())
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build();
    }

    @Bean
    public Kernel getSemanticKernelBean(ChatCompletionService chatCompletionService) {
        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .build();
    }

    @Bean
    public ChatHistory chatHistory() {
        return new ChatHistory();
    }
}
