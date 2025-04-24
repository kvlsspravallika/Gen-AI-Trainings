package com.epam.trainings.gen_ai_task_6.plugins.model;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Getter
@Slf4j
@Service
public class UserContext {
    private final UUID cartId;

    public UserContext() {
        this.cartId = UUID.randomUUID(); // Each user gets a unique cart ID
    }

}
