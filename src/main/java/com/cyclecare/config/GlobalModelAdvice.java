package com.cyclecare.config;

import com.cyclecare.domain.User;
import com.cyclecare.service.UserService;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.Year;

@ControllerAdvice(annotations = Controller.class)
public class GlobalModelAdvice {

    private final UserService userService;

    public GlobalModelAdvice(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userService.getCurrentUser(authentication);
    }

    @ModelAttribute("currentYear")
    public int currentYear() {
        return Year.now().getValue();
    }
}
