package com.epam.trainings.gen_ai_task_6.plugins.service;

import com.epam.trainings.gen_ai_task_6.plugins.model.cart.Cart;
import com.epam.trainings.gen_ai_task_6.plugins.model.cart.CartDelta;
import com.epam.trainings.gen_ai_task_6.plugins.model.cart.CheckoutResponse;
import com.epam.trainings.gen_ai_task_6.plugins.model.enums.PizzaSize;
import com.epam.trainings.gen_ai_task_6.plugins.model.enums.PizzaToppings;
import com.epam.trainings.gen_ai_task_6.plugins.model.pizza.Pizza;
import com.epam.trainings.gen_ai_task_6.plugins.model.pizza.PizzaMenu;
import com.epam.trainings.gen_ai_task_6.plugins.model.pizza.RemovePizzaResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PizzaService {

    private final Map<UUID, Cart> carts = new HashMap<>();

    public Mono<PizzaMenu> getMenu(){
        return Mono.just(new PizzaMenu());
    }

    public Mono<CartDelta> addPizzaToCart(UUID cartId, PizzaSize size, List<PizzaToppings> toppings, int quantity, String specialInstructions) {
        Cart cart = carts.computeIfAbsent(cartId, k -> new Cart());
        Pizza pizza = new Pizza(size, toppings, quantity, specialInstructions);
        cart.addPizza(pizza);
        return Mono.just(new CartDelta(pizza, cart));
    }

    public Mono<RemovePizzaResponse> removePizzaFromCart(UUID cartId, int pizzaId) {
        Cart cart = carts.get(cartId);
        if (cart != null) {
            boolean removed = cart.removePizza(pizzaId);
            return Mono.just(new RemovePizzaResponse(removed));
        }
        return Mono.just(new RemovePizzaResponse(false));
    }

    public Mono<Pizza> getPizzaFromCart(UUID cartId, int pizzaId) {
        Cart cart = carts.get(cartId);
        if (cart != null) {
            return Mono.justOrEmpty(cart.getPizza(pizzaId));
        }
        return Mono.empty();
    }

    public Mono<Cart> getCart(UUID cartId) {
        return Mono.justOrEmpty(carts.getOrDefault(cartId, new Cart()));
    }

    public Mono<CheckoutResponse> checkout(UUID cartId, String paymentId) {
        Cart cart = carts.remove(cartId);
        if (cart != null && !cart.getItems().isEmpty()) {
            return Mono.just(new CheckoutResponse(true, paymentId, cart.calculateTotal()));
        }
        return Mono.just(new CheckoutResponse(false, paymentId, 0));
    }
}
