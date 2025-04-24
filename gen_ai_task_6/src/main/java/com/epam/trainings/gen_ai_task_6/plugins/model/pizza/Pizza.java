package com.epam.trainings.gen_ai_task_6.plugins.model.pizza;

import com.epam.trainings.gen_ai_task_6.plugins.model.enums.PizzaSize;
import com.epam.trainings.gen_ai_task_6.plugins.model.enums.PizzaToppings;
import lombok.Getter;

import java.util.List;

@Getter
public class Pizza {

    private static int idCounter = 1;
    private final int id;
    private final PizzaSize size;
    private final List<PizzaToppings> toppings;
    private final int quantity;
    private final String specialInstructions;
    private final int price;

    public Pizza(PizzaSize size, List<PizzaToppings> toppings, int quantity, String specialInstructions) {
        this.id = idCounter++;
        this.size = size;
        this.toppings = toppings;
        this.quantity = quantity;
        this.specialInstructions = specialInstructions;
        this.price = 10 * quantity;
    }
}
