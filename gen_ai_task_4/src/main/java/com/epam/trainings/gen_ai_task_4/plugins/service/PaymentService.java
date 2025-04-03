package com.epam.trainings.gen_ai_task_4.plugins.service;

import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class PaymentService {

    public Mono<String> requestPaymentFromUser(UUID cartId) {
        return Mono.just(UUID.randomUUID().toString());
    }
}
