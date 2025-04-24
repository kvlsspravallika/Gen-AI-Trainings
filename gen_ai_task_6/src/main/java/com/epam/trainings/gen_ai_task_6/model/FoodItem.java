package com.epam.trainings.gen_ai_task_6.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FoodItem {

    private String name;

    private String description;

    private String cuisine;

    private Double price;

    private Double rating;

    private boolean isVegetarian;
}
