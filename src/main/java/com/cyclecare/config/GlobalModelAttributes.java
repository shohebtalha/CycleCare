package com.cyclecare.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    @ModelAttribute("currentPage")
    public String currentPage(HttpServletRequest request) {

        String uri = request.getRequestURI();

        if (uri.startsWith("/dashboard")) return "dashboard";
        if (uri.startsWith("/journal")) return "journal";
        if (uri.startsWith("/cycles")) return "cycles";
        if (uri.startsWith("/symptoms")) return "symptoms";
        if (uri.startsWith("/moods")) return "moods";
        if (uri.startsWith("/wellness")) return "wellness";
        if (uri.startsWith("/calendar")) return "calendar";
        if (uri.startsWith("/analytics")) return "analytics";
        if (uri.startsWith("/reports")) return "reports";
        if (uri.startsWith("/assistant")) return "assistant";

        return "";
    }
}