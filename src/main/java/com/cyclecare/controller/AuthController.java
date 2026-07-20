package com.cyclecare.controller;

import com.cyclecare.dto.ForgotPasswordDto;
import com.cyclecare.dto.RegistrationDto;
import com.cyclecare.dto.ResetPasswordDto;
import com.cyclecare.service.EmailVerificationService;
import com.cyclecare.service.PasswordResetService;
import com.cyclecare.service.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final UserService userService;
    private final PasswordResetService passwordResetService;
    private final EmailVerificationService emailVerificationService;

    public AuthController(UserService userService,
                          PasswordResetService passwordResetService,
                          EmailVerificationService emailVerificationService) {
        this.userService = userService;
        this.passwordResetService = passwordResetService;
        this.emailVerificationService = emailVerificationService;
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
        redirectAttributes.addFlashAttribute("success",
                "Registration successful. Check your email to verify and activate your account.");
        return "redirect:/login";
    }

    @GetMapping("/verify-email")
    public String verifyEmail(@RequestParam(required = false) String token,
                              RedirectAttributes redirectAttributes) {
        try {
            emailVerificationService.verifyEmail(token);
            redirectAttributes.addFlashAttribute("success", "Email verified. You can sign in now.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
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
        try {
            passwordResetService.requestPasswordReset(forgotPasswordDto.getEmail());
            redirectAttributes.addFlashAttribute("success",
                    "If an account exists for this email, a password reset link has been sent.");
        } catch (Exception e) {
            logger.error("Password reset email could not be sent.", e);
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Password reset email could not be sent right now."
            );
        }
        return "redirect:/forgot-password";
    }

    @GetMapping("/reset-password")
    public String resetPassword(@RequestParam(required = false) String token, Model model) {
        ResetPasswordDto dto = new ResetPasswordDto();
        dto.setToken(token);
        model.addAttribute("resetPasswordDto", dto);
        try {
            passwordResetService.validateResetToken(token);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
        }
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
            passwordResetService.resetPassword(resetPasswordDto);
        } catch (IllegalArgumentException ex) {
            bindingResult.rejectValue("token", "invalid", ex.getMessage());
            return "auth/reset-password";
        }
        redirectAttributes.addFlashAttribute("success", "Password updated. Please sign in with your new password.");
        return "redirect:/login";
    }
}
