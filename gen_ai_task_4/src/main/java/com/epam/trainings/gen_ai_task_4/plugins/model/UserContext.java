package com.epam.trainings.gen_ai_task_4.plugins.model;

import jakarta.servlet.http.HttpSession;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
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
