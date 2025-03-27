package com.epam.trainings.gen_ai_task_4.plugins;

import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * WeatherForecastPlugin is a class that provides weather forecasting functionality.
 * It simulates weather conditions, a random season, and temperature values
 * for a given city.
 */
@Slf4j
public class WeatherForecastPlugin {

    private final List<String> weatherConditions = new ArrayList<>();

    public WeatherForecastPlugin() {
        initializeWeatherConditions();
    }

    private void initializeWeatherConditions() {
        log.info("Initializing weather conditions...");
        weatherConditions.add("sunny");
        weatherConditions.add("rainy");
        weatherConditions.add("cloudy");
        weatherConditions.add("stormy");
        weatherConditions.add("windy");
        weatherConditions.add("snowy");
    }

    @DefineKernelFunction(name = "getWeatherForecast", description = "Returns weather forecast for given city with random season and temperature")
    public String getWeatherForecast(String city) {
        log.info("Getting weather forecast for city: {}", city);
        int temp = (int) (Math.random() * 30);  // Random temperature between 0 and 30°C
        String condition = weatherConditions.get((int) (Math.random() * weatherConditions.size()));
        return "Weather in the " + city + " is " + condition + " with " + temp + " degrees.";
    }

}
