package com.epam.trainings.gen_ai_task_3.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AzureOpenAIProperties.class)
public class SemanticKernelConfig {

    @Bean
    @ConditionalOnClass(OpenAIAsyncClient.class)
    @ConditionalOnMissingBean
    public OpenAIAsyncClient getOpenAIAsyncClientBean(AzureOpenAIProperties azureOpenAIProperties) {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(azureOpenAIProperties.getKey()))
                .endpoint(azureOpenAIProperties.getEndpoint())
                .buildAsyncClient();
    }

    @Bean
    public ChatCompletionService getChatCompletionServiceBean(OpenAIAsyncClient openAIAsyncClient,
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
