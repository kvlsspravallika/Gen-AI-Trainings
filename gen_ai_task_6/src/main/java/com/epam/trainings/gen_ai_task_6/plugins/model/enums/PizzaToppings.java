package com.epam.trainings.gen_ai_task_6.plugins.model.enums;

import lombok.Getter;

@Getter
public enum PizzaToppings {

    PEPPERONI("Pepperoni"),
    MUSHROOMS("Mushrooms"),
    ONIONS("Onions"),
    SAUSAGE("Sausage"),
    BACON("Bacon"),
    EXTRA_CHEESE("Extra Cheese"),
    BLACK_OLIVES("Black Olives"),
    GREEN_PEPPERS("Green Peppers"),
    PINEAPPLE("Pineapple"),
    SPINACH("Spinach");

    private final String displayName;

    PizzaToppings(String displayName) {
        this.displayName = displayName;
    }
}
