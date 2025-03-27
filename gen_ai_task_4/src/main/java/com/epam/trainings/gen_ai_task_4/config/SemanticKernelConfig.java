package com.epam.trainings.gen_ai_task_4.config;

import com.azure.ai.openai.OpenAIAsyncClient;
import com.azure.ai.openai.OpenAIClientBuilder;
import com.azure.core.credential.AzureKeyCredential;
import com.epam.trainings.gen_ai_task_4.plugins.LightsPlugin;
import com.epam.trainings.gen_ai_task_4.plugins.TimePlugin;
import com.epam.trainings.gen_ai_task_4.plugins.WeatherForecastPlugin;
import com.microsoft.semantickernel.Kernel;
import com.microsoft.semantickernel.aiservices.openai.chatcompletion.OpenAIChatCompletion;
import com.microsoft.semantickernel.plugin.KernelPlugin;
import com.microsoft.semantickernel.plugin.KernelPluginFactory;
import com.microsoft.semantickernel.services.chatcompletion.ChatCompletionService;
import com.microsoft.semantickernel.services.chatcompletion.ChatHistory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AzureOpenAIProperties.class)
public class SemanticKernelConfig {

    /**
     * Provides a Spring bean for an instance of OpenAIAsyncClient configured for generic purposes.
     * The client is built using the credentials and endpoint information provided in the AzureOpenAIProperties.
     *
     * @param azureOpenAIProperties the Azure-specific configuration properties containing the
     *                               API key and endpoint for the OpenAI Async client.
     * @return an instance of OpenAIAsyncClient configured with the provided credentials and endpoint.
     */
    @Bean(name = "getGenericOpenAIAsyncClientBean")
    public OpenAIAsyncClient getGenericOpenAIAsyncClientBean(AzureOpenAIProperties azureOpenAIProperties) {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(azureOpenAIProperties.getGenericKey()))
                .endpoint(azureOpenAIProperties.getGenericEndPoint())
                .buildAsyncClient();
    }

    /**
     * Provides a Spring bean for an instance of OpenAIAsyncClient configured specifically for image generation purposes.
     * The client is built using the API key and endpoint information provided in the AzureOpenAIProperties configuration.
     *
     * @param azureOpenAIProperties the configuration properties that include the API key and endpoint
     *                               for image generation using the OpenAI Async client.
     * @return an instance of OpenAIAsyncClient configured with the provided credentials and endpoint for image generation.
     */
    @Bean(name = "getOpenAIAsyncClientBeanForImageGeneration")
    public OpenAIAsyncClient getOpenAIAsyncClientBeanForImageGeneration(AzureOpenAIProperties azureOpenAIProperties) {
        return new OpenAIClientBuilder()
                .credential(new AzureKeyCredential(azureOpenAIProperties.getImageGenerationKey()))
                .endpoint(azureOpenAIProperties.getImageGenerationEndpoint())
                .buildAsyncClient();
    }

    /**
     * Provides a Spring bean for an instance of ChatCompletionService configured with the specified
     * OpenAIAsyncClient and AzureOpenAIProperties. The service is set up using the deployment name
     * from the provided AzureOpenAIProperties and the given OpenAIAsyncClient.
     *
     * @param openAIAsyncClient an instance of OpenAIAsyncClient configured for generic purposes,
     *                          injected from the Spring context.
     * @param azureOpenAIProperties configuration properties containing OpenAI deployment information,
     *                               including the deployment name to be used.
     * @return an instance of ChatCompletionService configured with the specified OpenAIAsyncClient
     *         and deployment name from AzureOpenAIProperties.
     */
    @Bean
    public ChatCompletionService getChatCompletionServiceBean(@Qualifier("getGenericOpenAIAsyncClientBean") OpenAIAsyncClient openAIAsyncClient,
                                                              AzureOpenAIProperties azureOpenAIProperties) {
        return OpenAIChatCompletion.builder()
                .withModelId(azureOpenAIProperties.getDeploymentName())
                .withOpenAIAsyncClient(openAIAsyncClient)
                .build();
    }

    /**
     * Provides a Spring bean for an instance of Kernel configured with the specified
     * ChatCompletionService. The Kernel is built using the AI service handler provided
     * by the ChatCompletionService instance.
     *
     * @param chatCompletionService an instance of ChatCompletionService used to configure
     *                              the Kernel with AI service capabilities.
     * @return an instance of Kernel configured with the specified ChatCompletionService.
     */
    @Bean
    public Kernel getSemanticKernelBean(ChatCompletionService chatCompletionService) {

        // Import the LightsPlugin
        KernelPlugin lightPlugin = KernelPluginFactory.createFromObject(new LightsPlugin(),
                "LightsPlugin");
        KernelPlugin timePlugin = KernelPluginFactory.createFromObject(new TimePlugin(), "TimePlugin");
        KernelPlugin weatherPlugin = KernelPluginFactory.createFromObject(new WeatherForecastPlugin(), "WeatherPlugin");

        return Kernel.builder()
                .withAIService(ChatCompletionService.class, chatCompletionService)
                .withPlugin(lightPlugin)
                .withPlugin(timePlugin)
                .withPlugin(weatherPlugin)
                .build();
    }

    /**
     * Provides a Spring bean for an instance of ChatHistory.
     * The ChatHistory bean can be used to manage and store conversation history.
     *
     * @return an instance of ChatHistory.
     */
    @Bean
    public ChatHistory chatHistory() {
        return new ChatHistory();
    }
}
