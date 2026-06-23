package com.cyclecare.controller;

import com.cyclecare.dto.ForgotPasswordDto;
import com.cyclecare.dto.RegistrationDto;
import com.cyclecare.dto.ResetPasswordDto;
import com.cyclecare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return "redirect:/dashboard";
        }
        return "auth/login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registrationDto", new RegistrationDto());
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(@Valid @ModelAttribute RegistrationDto registrationDto,
                           BindingResult bindingResult,
                           RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/register";
        }
        try {
            userService.register(registrationDto);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("email", "duplicate", ex.getMessage());
            return "auth/register";
        }
        redirectAttributes.addFlashAttribute("success", "Registration successful. You can sign in now.");
        return "redirect:/login";
    }

    @GetMapping("/forgot-password")
    public String forgotPassword(Model model) {
        model.addAttribute("forgotPasswordDto", new ForgotPasswordDto());
        return "auth/forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotPassword(@Valid @ModelAttribute ForgotPasswordDto forgotPasswordDto,
                                 BindingResult bindingResult,
                                 RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/forgot-password";
        }
        String token = userService.createPasswordResetToken(forgotPasswordDto.getEmail());
        redirectAttributes.addFlashAttribute("success",
                "If that email exists, a reset link has been prepared.");
        if (token != null) {
            redirectAttributes.addFlashAttribute("resetLink", "/reset-password?token=" + token);
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setToken(token);
        model.addAttribute("resetPasswordDto", dto);
        return "auth/reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(@Valid @ModelAttribute ResetPasswordDto resetPasswordDto,
                                BindingResult bindingResult,
                                RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            return "auth/reset-password";
        }
        try {
            userService.resetPassword(resetPasswordDto);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("token", "invalid", ex.getMessage());
            return "auth/reset-password";
        }
        redirectAttributes.addFlashAttribute("success", "Password updated. Please sign in with your new password.");
        return "redirect:/login";
    }
}
