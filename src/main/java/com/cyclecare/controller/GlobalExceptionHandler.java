package com.cyclecare.controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public String handleApplicationErrors(RuntimeException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/application-error";
    }
}
