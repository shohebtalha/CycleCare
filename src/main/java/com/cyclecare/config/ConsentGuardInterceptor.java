package com.cyclecare.config;

import com.cyclecare.domain.User;
import com.cyclecare.service.ConsentService;
import com.cyclecare.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Set;

@Component
public class ConsentGuardInterceptor implements HandlerInterceptor {

    private static final Set<String> ALLOWED_PREFIXES = Set.of(
            "/consent/review", "/privacy", "/terms", "/consent", "/logout", "/css/", "/js/", "/images/", "/webjars/"
    );

    private final UserService userService;
    private final ConsentService consentService;

    public ConsentGuardInterceptor(UserService userService, ConsentService consentService) {
        this.userService = userService;
        this.consentService = consentService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (ALLOWED_PREFIXES.stream().anyMatch(path::startsWith)) {
            return true;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return true;
        }

        User user = userService.getCurrentUser(authentication);
        if (consentService.hasCurrentConsent(user)) {
            return true;
        }

        response.sendRedirect(request.getContextPath() + "/consent/review");
        return false;
    }
}
