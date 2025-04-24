package com.epam.trainings.gen_ai_task_6.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class DeploymentModel {

    List<String> textModels;

    List<String> imageModels;

    List<String> textEmbeddingModels;
}
