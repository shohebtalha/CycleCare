package com.cyclecare.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Handles exceptions thrown by MVC @Controller classes (page-rendering endpoints).
 * REST endpoints under /api/** are handled separately by {@link GlobalApiExceptionHandler}.
 */
@ControllerAdvice(annotations = Controller.class)
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String handleApplicationErrors(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/application-error";
    }
}

