package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.dto.CycleDto;
import com.cyclecare.service.CycleService;
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
public class CycleController {

    private final UserService userService;
    private final CycleService cycleService;

    public CycleController(UserService userService, CycleService cycleService) {
        this.userService = userService;
        this.cycleService = cycleService;
    }

    @GetMapping("/cycles")
    public String cycles(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("cycleDto", new CycleDto());
        model.addAttribute("cycles", cycleService.allCycles(user));
        model.addAttribute("prediction", cycleService.currentPrediction(user).orElse(null));
        return "cycles";
    }

    @PostMapping("/cycles")
    public String saveCycle(Authentication authentication,
                            @Valid @ModelAttribute CycleDto cycleDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        if (bindingResult.hasErrors()) {
            model.addAttribute("cycles", cycleService.allCycles(user));
            model.addAttribute("prediction", cycleService.currentPrediction(user).orElse(null));
            return "cycles";
        }
        cycleService.saveCycle(user, cycleDto);
        redirectAttributes.addFlashAttribute("success", "Cycle details saved.");
        return "redirect:/cycles";
    }
}
