package com.epam.trainings.gen_ai_task_6.plugins;

import com.epam.trainings.gen_ai_task_6.plugins.model.LightsModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * LightsPlugin is a plugin that provides functionality to manage and manipulate
 * a collection of mock light models. It includes methods to retrieve the list
 * of lights and their current state, as well as to update the state of a specific
 * light.
 */
@Slf4j
public class LightsPlugin {

    // Mock data for the lights
    private final Map<Integer, LightsModel> lights = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LightsPlugin() {
        lights.put(1, new LightsModel(1, "Table Lamp", false, LightsModel.Brightness.MEDIUM, "#FFFFFF"));
        lights.put(2, new LightsModel(2, "Porch light", false, LightsModel.Brightness.HIGH, "#FF0000"));
        lights.put(3, new LightsModel(3, "Chandelier", true, LightsModel.Brightness.LOW, "#FFFF00"));
    }

    @DefineKernelFunction(name = "get_lights", description = "Gets a list of lights and their current state")
    public Collection<LightsModel> getLights() {
//        log.info("Getting lights...");
//        String result = lights.values().stream().map(LightsModel::getName).collect(Collectors.joining(","));
//        log.info("Fetched lights: {}", result);
//        return result;
        log.info("Getting lights...");
        Collection<LightsModel> result = lights.values();
        log.info("Fetched lights: {}", result);
        return result;
    }

    @DefineKernelFunction(name = "change_state", description = "Changes the state of the light")
    public LightsModel changeState(
            @KernelFunctionParameter(name = "model", description = "The new state of the model to set. Example model: " +
                    "{\"id\":99,\"name\":\"Head Lamp\",\"isOn\":false,\"brightness\":\"MEDIUM\",\"color\":\"#FFFFFF\"}") String modelJson) {
//        try {
//            log.info("Changing state of light...");
//            LightsModel model = objectMapper.readValue(modelJson, LightsModel.class);
//            log.info("Changing state of light: {}", model);
//            if (!lights.containsKey(model.getId())) {
//                return "Error: Light not found";
//            }
//
//            lights.put(model.getId(), model);
//            log.info("Changed state of light: {}", lights.get(model.getId()));
//            return objectMapper.writeValueAsString(lights.get(model.getId()));
//
//        } catch (Exception e) {
//            return "Error processing LightsModel: " + e.getMessage();
//        }
        try {
            log.info("Changing state of light...");

            // Deserialize input JSON into LightsModel
            LightsModel lightsModel = objectMapper.readValue(modelJson, LightsModel.class);

            // Validate the deserialized LightsModel
            if (lightsModel == null) {
                throw new IllegalArgumentException("Invalid input: LightsModel cannot be null");
            }

            if (!lights.containsKey(lightsModel.getId())) {
                throw new IllegalArgumentException("Light with ID " + lightsModel.getId() + " not found");
            }

            // Update the light's state
            LightsModel existingLight = lights.get(lightsModel.getId());
            existingLight.setIsOn(lightsModel.getIsOn());
            existingLight.setBrightness(lightsModel.getBrightness());
            existingLight.setColor(lightsModel.getColor());

            log.info("Changed state of light: {}", existingLight);
            return existingLight;

        } catch (Exception e) {
            // Handle errors during deserialization or processing
            log.error("Error processing LightsModel: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to change light state: " + e.getMessage(), e);
        }

    }

}
