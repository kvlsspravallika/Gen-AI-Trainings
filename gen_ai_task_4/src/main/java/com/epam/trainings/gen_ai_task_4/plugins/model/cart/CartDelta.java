package com.epam.trainings.gen_ai_task_4.plugins.model.cart;

import com.epam.trainings.gen_ai_task_4.plugins.model.pizza.Pizza;

public class CartDelta {

    private final Pizza newItem;
    private final Cart updatedCart;

    public CartDelta(Pizza newItem, Cart updatedCart) {
        this.newItem = newItem;
        this.updatedCart = updatedCart;
    }
}
