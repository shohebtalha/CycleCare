package com.cyclecare.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final boolean googleOAuthConfigured;

    public GlobalModelAttributes(@Qualifier("googleOAuthConfigured") boolean googleOAuthConfigured) {
        this.googleOAuthConfigured = googleOAuthConfigured;
    }

    @ModelAttribute("googleOAuthConfigured")
    public boolean googleOAuthConfigured() {
        return googleOAuthConfigured;
    }

    @ModelAttribute("currentPage")
    public String currentPage(HttpServletRequest request) {

        String uri = request.getRequestURI();

        if (uri.startsWith("/login")) return "login";
        if (uri.startsWith("/register")) return "register";
        if (uri.startsWith("/forgot-password")) return "forgot-password";
        if (uri.startsWith("/reset-password")) return "reset-password";
        if (uri.startsWith("/privacy")) return "privacy";
        if (uri.startsWith("/terms")) return "terms";
        if (uri.startsWith("/consent")) return "consent";
        if (uri.startsWith("/dashboard")) return "dashboard";
        if (uri.startsWith("/journal")) return "journal";
        if (uri.startsWith("/cycles")) return "cycles";
        if (uri.startsWith("/symptoms")) return "symptoms";
        if (uri.startsWith("/moods")) return "moods";
        if (uri.startsWith("/wellness")) return "wellness";
        if (uri.startsWith("/calendar")) return "calendar";
        if (uri.startsWith("/analytics")) return "analytics";
        if (uri.startsWith("/health-intelligence")) return "health-intelligence";
        if (uri.startsWith("/reports")) return "reports";
        if (uri.startsWith("/assistant")) return "assistant";

        return "";
    }
}
