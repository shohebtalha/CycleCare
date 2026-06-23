package com.cyclecare.controller;

import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.SleepQuality;
import com.cyclecare.domain.User;
import com.cyclecare.dto.SleepLogDto;
import com.cyclecare.dto.WaterLogDto;
import com.cyclecare.service.CyclePrediction;
import com.cyclecare.service.CycleService;
import com.cyclecare.service.RecommendationService;
import com.cyclecare.service.SleepService;
import com.cyclecare.service.UserService;
import com.cyclecare.service.WaterService;
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
public class WellnessController {

    private final UserService userService;
    private final CycleService cycleService;
    private final RecommendationService recommendationService;
    private final WaterService waterService;
    private final SleepService sleepService;

    public WellnessController(UserService userService,
                              CycleService cycleService,
                              RecommendationService recommendationService,
                              WaterService waterService,
                              SleepService sleepService) {
        this.userService = userService;
        this.cycleService = cycleService;
        this.recommendationService = recommendationService;
        this.waterService = waterService;
        this.sleepService = sleepService;
    }

    @GetMapping("/wellness")
    public String wellness(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        populate(model, user, new WaterLogDto(), new SleepLogDto());
        return "wellness";
    }

    @PostMapping("/water")
    public String saveWater(Authentication authentication,
                            @Valid @ModelAttribute WaterLogDto waterLogDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        if (bindingResult.hasErrors()) {
            populate(model, user, waterLogDto, new SleepLogDto());
            return "wellness";
        }
        waterService.save(user, waterLogDto);
        redirectAttributes.addFlashAttribute("success", "Water intake logged.");
        return "redirect:/wellness";
    }

    @PostMapping("/sleep")
    public String saveSleep(Authentication authentication,
                            @Valid @ModelAttribute SleepLogDto sleepLogDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        if (bindingResult.hasErrors()) {
            populate(model, user, new WaterLogDto(), sleepLogDto);
            return "wellness";
        }
        sleepService.save(user, sleepLogDto);
        redirectAttributes.addFlashAttribute("success", "Sleep entry logged.");
        return "redirect:/wellness";
    }

    private void populate(Model model, User user, WaterLogDto waterLogDto, SleepLogDto sleepLogDto) {
        CyclePrediction prediction = cycleService.currentPrediction(user).orElse(null);
        MenstrualPhase phase = prediction != null ? prediction.getPhase() : MenstrualPhase.FOLLICULAR;
        model.addAttribute("prediction", prediction);
        model.addAttribute("phase", phase);
        model.addAttribute("nutrition", recommendationService.nutritionForPhase(phase));
        model.addAttribute("exercises", recommendationService.exercisesForPhase(phase));
        model.addAttribute("waterLogDto", waterLogDto);
        model.addAttribute("sleepLogDto", sleepLogDto);
        model.addAttribute("sleepQualities", SleepQuality.values());
        model.addAttribute("waterToday", waterService.totalForToday(user));
        model.addAttribute("waterLogs", waterService.recent(user));
        model.addAttribute("sleepAverage", sleepService.weeklyAverage(user));
        model.addAttribute("sleepLogs", sleepService.recent(user));
    }
}
