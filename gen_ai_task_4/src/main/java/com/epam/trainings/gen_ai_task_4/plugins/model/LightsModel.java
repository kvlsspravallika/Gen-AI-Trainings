package com.epam.trainings.gen_ai_task_4.plugins.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LightsModel {

    private int id;
    private String name;
    private Boolean isOn;
    private Brightness brightness;
    private String color;


    public enum Brightness {
        LOW,
        MEDIUM,
        HIGH
    }
}
