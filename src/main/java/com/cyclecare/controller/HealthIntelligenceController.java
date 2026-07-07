package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.service.HealthIntelligenceService;
import com.cyclecare.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HealthIntelligenceController {

    private final UserService userService;
    private final HealthIntelligenceService healthIntelligenceService;

    public HealthIntelligenceController(UserService userService,
                                        HealthIntelligenceService healthIntelligenceService) {
        this.userService = userService;
        this.healthIntelligenceService = healthIntelligenceService;
    }

    @GetMapping("/health-intelligence")
    public String healthIntelligence(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("intelligence", healthIntelligenceService.build(user));
        return "health-intelligence";
    }
}
