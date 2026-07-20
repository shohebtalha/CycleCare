package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.dto.ConsentAcceptanceDto;
import com.cyclecare.service.ConsentService;
import com.cyclecare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ConsentController {

    private final ConsentService consentService;
    private final UserService userService;

    public ConsentController(ConsentService consentService, UserService userService) {
        this.consentService = consentService;
        this.userService = userService;
    }

    @GetMapping("/consent/review")
    public String review(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("consentAcceptanceDto", new ConsentAcceptanceDto());
        model.addAttribute("policyVersion", consentService.currentVersion());
        model.addAttribute("latestConsent", consentService.latestConsent(user).orElse(null));
        return "legal/review-consent";
    }

    @PostMapping("/consent/review")
    public String accept(Authentication authentication,
                         @Valid @ModelAttribute ConsentAcceptanceDto consentAcceptanceDto,
                         BindingResult bindingResult,
                         Model model,
                         RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("policyVersion", consentService.currentVersion());
        model.addAttribute("latestConsent", consentService.latestConsent(user).orElse(null));
        if (bindingResult.hasErrors()) {
            return "legal/review-consent";
        }

        consentService.recordCurrentConsent(user, consentAcceptanceDto);
        redirectAttributes.addFlashAttribute("success", "Your privacy choices have been recorded.");
        return "redirect:/dashboard";
    }
}
