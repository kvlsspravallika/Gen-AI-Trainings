package com.epam.trainings.gen_ai_task_6.plugins.model.cart;

import com.epam.trainings.gen_ai_task_6.plugins.model.pizza.Pizza;

import java.util.ArrayList;
import java.util.List;

public class Cart {
    private final List<Pizza> items = new ArrayList<>();

    public void addPizza(Pizza pizza) {
        items.add(pizza);
    }

    public boolean removePizza(int pizzaId) {
        return items.removeIf(pizza -> pizza.getId() == pizzaId);
    }

    public Pizza getPizza(int pizzaId) {
        return items.stream().filter(p -> p.getId() == pizzaId).findFirst().orElse(null);
    }

    public List<Pizza> getItems() {
        return items;
    }

    public int calculateTotal() {
        return items.stream().mapToInt(Pizza::getPrice).sum();
    }
}
