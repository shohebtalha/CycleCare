package com.cyclecare.controller;

import com.cyclecare.domain.ActivityLevel;
import com.cyclecare.domain.User;
import com.cyclecare.dto.ProfileDto;
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
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("profileDto", userService.toProfileDto(user));
        model.addAttribute("activityLevels", ActivityLevel.values());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @Valid @ModelAttribute ProfileDto profileDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        model.addAttribute("activityLevels", ActivityLevel.values());
        if (bindingResult.hasErrors()) {
            return "profile";
        }
        userService.updateProfile(userService.getCurrentUser(authentication), profileDto);
        redirectAttributes.addFlashAttribute("success", "Profile updated.");
        return "redirect:/profile";
    }
}
