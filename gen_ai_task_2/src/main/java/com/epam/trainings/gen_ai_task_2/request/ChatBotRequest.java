package com.epam.trainings.gen_ai_task_2.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Builder
@Data
@NoArgsConstructor
public class ChatBotRequest {

    private String userPrompt;

    private Double temperature;

    private Integer maxTokens;
}
