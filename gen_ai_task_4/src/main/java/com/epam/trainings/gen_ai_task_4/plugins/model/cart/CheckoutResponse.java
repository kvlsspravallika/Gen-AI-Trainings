package com.epam.trainings.gen_ai_task_4.plugins.model.cart;

public class CheckoutResponse {

    private final boolean success;
    private final String paymentId;
    private final int total;

    public CheckoutResponse(boolean success, String paymentId, int total) {
        this.success = success;
        this.paymentId = paymentId;
        this.total = total;
    }
}
