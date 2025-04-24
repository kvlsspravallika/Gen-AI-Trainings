package com.epam.trainings.gen_ai_task_6.plugins.model.enums;

import lombok.Getter;

@Getter
public enum PizzaSize {

    SMALL("Small"),
    MEDIUM("Medium"),
    LARGE("Large");

    private final String displayName;

    PizzaSize(String displayName) {
        this.displayName = displayName;
    }
}
