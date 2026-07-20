package com.cyclecare.controller;

import com.cyclecare.service.ConsentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LegalController {

    private final ConsentService consentService;

    public LegalController(ConsentService consentService) {
        this.consentService = consentService;
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {
        model.addAttribute("policyVersion", consentService.currentVersion());
        return "legal/privacy";
    }

    @GetMapping("/terms")
    public String terms(Model model) {
        model.addAttribute("policyVersion", consentService.currentVersion());
        return "legal/terms";
    }

    @GetMapping("/consent")
    public String consent(Model model) {
        model.addAttribute("policyVersion", consentService.currentVersion());
        return "legal/consent";
    }
}
