package com.epam.trainings.gen_ai_task_6.model.response;

import io.qdrant.client.grpc.JsonWithInt;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PointResponse {

    private String pointId;
    private Map<String, JsonWithInt.Value> payloadMap;
    private List<Float> vectors;
}
