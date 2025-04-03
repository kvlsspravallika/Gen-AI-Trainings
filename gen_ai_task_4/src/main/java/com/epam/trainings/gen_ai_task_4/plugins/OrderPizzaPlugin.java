package com.epam.trainings.gen_ai_task_4.plugins;

import com.epam.trainings.gen_ai_task_4.plugins.model.UserContext;
import com.epam.trainings.gen_ai_task_4.plugins.model.cart.Cart;
import com.epam.trainings.gen_ai_task_4.plugins.model.cart.CartDelta;
import com.epam.trainings.gen_ai_task_4.plugins.model.cart.CheckoutResponse;
import com.epam.trainings.gen_ai_task_4.plugins.model.enums.PizzaSize;
import com.epam.trainings.gen_ai_task_4.plugins.model.enums.PizzaToppings;
import com.epam.trainings.gen_ai_task_4.plugins.model.pizza.Pizza;
import com.epam.trainings.gen_ai_task_4.plugins.model.pizza.PizzaMenu;
import com.epam.trainings.gen_ai_task_4.plugins.model.pizza.RemovePizzaResponse;
import com.epam.trainings.gen_ai_task_4.plugins.service.PaymentService;
import com.epam.trainings.gen_ai_task_4.plugins.service.PizzaService;
import com.microsoft.semantickernel.semanticfunctions.annotations.DefineKernelFunction;
import com.microsoft.semantickernel.semanticfunctions.annotations.KernelFunctionParameter;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

@Slf4j
public class OrderPizzaPlugin {

    private final PizzaService pizzaService;
    private final UserContext userContext;
    private final PaymentService paymentService;

    public OrderPizzaPlugin(PizzaService pizzaService, UserContext userContext, PaymentService paymentService) {
        this.pizzaService = pizzaService;
        this.userContext = userContext;
        this.paymentService = paymentService;
    }

    @DefineKernelFunction(name = "getPizzaMenu", description = "Get the pizza menu.", returnType = "com.epam.trainings.gen_ai_task_4.plugins.model.pizza.PizzaMenu")
    public Mono<PizzaMenu> getPizzaMenuAsync(){
        log.info("fetching menu for pizza......");
        return pizzaService.getMenu();
    }

    @DefineKernelFunction(
            name = "add_pizza_to_cart",
            description = "Add a pizza to the user's cart",
            returnDescription = "Returns the new item and updated cart",
            returnType = "com.epam.trainings.gen_ai_task_4.plugins.model.cart.CartDelta")
    public Mono<CartDelta> addPizzaToCart(
            @KernelFunctionParameter(name = "size", description = "The size of the pizza", type = com.epam.trainings.gen_ai_task_4.plugins.model.enums.PizzaSize.class, required = true)
            PizzaSize size,
            @KernelFunctionParameter(name = "toppings", description = "The list of toppings to add to the the pizza", type = List.class)
            List<PizzaToppings> toppings,
            @KernelFunctionParameter(name = "quantity", description = "How many of this pizza to order", type = Integer.class, defaultValue = "1")
            Integer quantity,
            @KernelFunctionParameter(name = "specialInstructions", description = "Special instructions for the order")
            String specialInstructions
    )
    {
        log.info("Adding pizza with toppings: {}", toppings);
        return pizzaService.addPizzaToCart(
                userContext.getCartId(),
                size,
                toppings,
                quantity,
                specialInstructions);
    }

    @DefineKernelFunction(name = "remove_pizza_from_cart", description = "Remove a pizza from the cart.", returnType = "com.epam.trainings.gen_ai_task_4.plugins.model.pizza.RemovePizzaResponse")
    public Mono<RemovePizzaResponse> removePizzaFromCart(
            @KernelFunctionParameter(name = "pizzaId", description = "Id of the pizza to remove from the cart", type = Integer.class, required = true)
            int pizzaId)
    {
        UUID cartId = userContext.getCartId();
        return pizzaService.removePizzaFromCart(cartId, pizzaId);
    }

    @DefineKernelFunction(
            name = "get_pizza_from_cart",
            description = "Returns the specific details of a pizza in the user's cart; use this instead of relying on previous messages since the cart may have changed since then.",
            returnType = "com.epam.trainings.gen_ai_task_4.plugins.model.pizza.Pizza")
    public Mono<Pizza> getPizzaFromCart(
            @KernelFunctionParameter(name = "pizzaId", description = "Id of the pizza to get from the cart", type = Integer.class, required = true)
            int pizzaId)
    {

        UUID cartId = userContext.getCartId();
        return pizzaService.getPizzaFromCart(cartId, pizzaId);
    }

    @DefineKernelFunction(
            name = "get_cart",
            description = "Returns the user's current cart, including the total price and items in the cart.",
            returnType = "com.epam.trainings.gen_ai_task_4.plugins.model.cart.Cart")

    public Mono<Cart> getCart()
    {
        UUID cartId = userContext.getCartId();
        return pizzaService.getCart(cartId);
    }


    @DefineKernelFunction(
            name = "checkout",
            description = "Checkouts the user's cart; this function will retrieve the payment from the user and complete the order.",
            returnType = "com.epam.trainings.gen_ai_task_4.plugins.model.cart.CheckoutResponse")
    public Mono<CheckoutResponse> Checkout()
    {
        UUID cartId = userContext.getCartId();
        return paymentService.requestPaymentFromUser(cartId)
                .flatMap(paymentId -> pizzaService.checkout(cartId, paymentId));
    }
}

