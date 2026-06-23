package com.cyclecare.controller;

import com.cyclecare.domain.SymptomType;
import com.cyclecare.domain.User;
import com.cyclecare.dto.SymptomDto;
import com.cyclecare.service.SymptomService;
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
public class SymptomController {

    private final UserService userService;
    private final SymptomService symptomService;

    public SymptomController(UserService userService, SymptomService symptomService) {
        this.userService = userService;
        this.symptomService = symptomService;
    }

    @GetMapping("/symptoms")
    public String symptoms(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        populate(model, user, new SymptomDto());
        return "symptoms";
    }

    @PostMapping("/symptoms")
    public String saveSymptom(Authentication authentication,
                              @Valid @ModelAttribute SymptomDto symptomDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        if (bindingResult.hasErrors()) {
            populate(model, user, symptomDto);
            return "symptoms";
        }
        symptomService.save(user, symptomDto);
        redirectAttributes.addFlashAttribute("success", "Symptom entry saved.");
        return "redirect:/symptoms";
    }

    private void populate(Model model, User user, SymptomDto dto) {
        model.addAttribute("symptomDto", dto);
        model.addAttribute("symptomTypes", SymptomType.values());
        model.addAttribute("symptoms", symptomService.history(user));
    }
}
