package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.dto.DashboardView;
import com.cyclecare.service.DashboardFacadeService;
import com.cyclecare.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final UserService userService;
    private final DashboardFacadeService dashboardFacade;

    public DashboardController(UserService userService, DashboardFacadeService dashboardFacade) {
        this.userService = userService;
        this.dashboardFacade = dashboardFacade;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        DashboardView view = dashboardFacade.buildDashboardForUser(user);

        model.addAttribute("prediction", view.prediction());
        model.addAttribute("menstrualDay", view.menstrualDay());
        model.addAttribute("latestSymptoms", view.latestSymptoms());
        model.addAttribute("latestMoods", view.latestMoods());
        model.addAttribute("waterToday", view.waterToday());
        model.addAttribute("sleepAverage", view.sleepAverage());
        model.addAttribute("insights", view.insights());
        model.addAttribute("nutrition", view.nutrition());
        model.addAttribute("exercises", view.exercises());
        model.addAttribute("todayFlow", view.todayFlow());
        model.addAttribute("todayFlowRecommendation", view.todayFlowRecommendation());

        return "dashboard";
    }
}
