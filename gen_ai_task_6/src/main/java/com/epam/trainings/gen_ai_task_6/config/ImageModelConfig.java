package com.epam.trainings.gen_ai_task_6.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "deployment.image")
@Getter
@Setter
public class ImageModelConfig {

    private List<String> models;
}
