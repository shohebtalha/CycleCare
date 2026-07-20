package com.cyclecare.controller;

import com.cyclecare.domain.ActivityLevel;
import com.cyclecare.domain.User;
import com.cyclecare.dto.AccountDeletionDto;
import com.cyclecare.dto.ChangePasswordDto;
import com.cyclecare.dto.ProfileDto;
import jakarta.servlet.http.HttpServletRequest;
import com.cyclecare.service.UserService;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpSession;
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
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        model.addAttribute("accountDeletionDto", new AccountDeletionDto());
        model.addAttribute("activityLevels", ActivityLevel.values());
        model.addAttribute("localPasswordAccount", user.getAuthProvider() == com.cyclecare.domain.AuthProvider.LOCAL);
        model.addAttribute("accountEmail", user.getEmail());
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(Authentication authentication,
                                @Valid @ModelAttribute ProfileDto profileDto,
                                BindingResult bindingResult,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        model.addAttribute("activityLevels", ActivityLevel.values());
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        model.addAttribute("accountDeletionDto", new AccountDeletionDto());
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("localPasswordAccount", user.getAuthProvider() == com.cyclecare.domain.AuthProvider.LOCAL);
        model.addAttribute("accountEmail", user.getEmail());
        if (bindingResult.hasErrors()) {
            return "profile";
        }
        userService.updateProfile(user, profileDto);
        redirectAttributes.addFlashAttribute("success", "Profile updated.");
        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String changePassword(Authentication authentication,
                                 @Valid @ModelAttribute ChangePasswordDto changePasswordDto,
                                 BindingResult bindingResult,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("profileDto", userService.toProfileDto(user));
        model.addAttribute("activityLevels", ActivityLevel.values());
        model.addAttribute("accountDeletionDto", new AccountDeletionDto());
        model.addAttribute("localPasswordAccount", user.getAuthProvider() == com.cyclecare.domain.AuthProvider.LOCAL);
        model.addAttribute("accountEmail", user.getEmail());
        if (bindingResult.hasErrors()) {
            return "profile";
        }
        try {
            userService.changePassword(user, changePasswordDto);
            redirectAttributes.addFlashAttribute("success", "Password updated.");
            return "redirect:/profile";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("currentPassword", "invalid", ex.getMessage());
            return "profile";
        }
    }

    @PostMapping("/profile/delete")
    public String deleteAccount(Authentication authentication,
                                @Valid @ModelAttribute AccountDeletionDto accountDeletionDto,
                                BindingResult bindingResult,
                                Model model,
                                HttpServletRequest request,
                                RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("profileDto", userService.toProfileDto(user));
        model.addAttribute("activityLevels", ActivityLevel.values());
        model.addAttribute("changePasswordDto", new ChangePasswordDto());
        model.addAttribute("localPasswordAccount", user.getAuthProvider() == com.cyclecare.domain.AuthProvider.LOCAL);
        model.addAttribute("accountEmail", user.getEmail());
        if (bindingResult.hasErrors()) {
            model.addAttribute("focusSection", "delete-account");
            return "profile";
        }
        try {
            userService.deleteAccount(user, accountDeletionDto.getPassword());
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.invalidate();
            }
            redirectAttributes.addFlashAttribute("success", "Your account and personal health data have been deleted.");
            return "redirect:/login";
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("password", "invalid", ex.getMessage());
            model.addAttribute("focusSection", "delete-account");
            return "profile";
        }
    }
}
