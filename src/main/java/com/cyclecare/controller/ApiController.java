package com.cyclecare.controller;

import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.User;
import com.cyclecare.dto.WaterLogDto;
import com.cyclecare.service.AnalyticsService;
import com.cyclecare.service.CalendarDay;
import com.cyclecare.service.CalendarService;
import com.cyclecare.service.CyclePrediction;
import com.cyclecare.service.CycleService;
import com.cyclecare.service.RecommendationService;
import com.cyclecare.service.UserService;
import com.cyclecare.service.WaterService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserService userService;
    private final CycleService cycleService;
    private final AnalyticsService analyticsService;
    private final CalendarService calendarService;
    private final RecommendationService recommendationService;
    private final WaterService waterService;

    public ApiController(UserService userService,
                         CycleService cycleService,
                         AnalyticsService analyticsService,
                         CalendarService calendarService,
                         RecommendationService recommendationService,
                         WaterService waterService) {
        this.userService = userService;
        this.cycleService = cycleService;
        this.analyticsService = analyticsService;
        this.calendarService = calendarService;
        this.recommendationService = recommendationService;
        this.waterService = waterService;
    }

    @GetMapping("/dashboard")
    public Map<String, Object> dashboard(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        CyclePrediction prediction = cycleService.currentPrediction(user).orElse(null);
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("name", user.getName());
        response.put("prediction", prediction);
        response.put("waterToday", waterService.totalForToday(user));
        response.put("regularityScore", analyticsService.regularityScore(user));
        response.put("insights", analyticsService.insights(user).stream()
                .map(insight -> Map.of("title", insight.getTitle(), "message", insight.getMessage(), "type", insight.getType()))
                .toList());
        return response;
    }

    @GetMapping("/cycle/current")
    public ResponseEntity<CyclePrediction> currentCycle(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        return cycleService.currentPrediction(user)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/analytics")
    public Map<String, Object> analytics(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        return Map.of(
                "cycleLengthTrend", analyticsService.cycleLengthTrend(user),
                "moodTrend", analyticsService.moodTrend(user),
                "symptomFrequency", analyticsService.symptomFrequency(user),
                "regularityScore", analyticsService.regularityScore(user)
        );
    }

    @GetMapping("/calendar")
    public List<CalendarDay> calendar(Authentication authentication,
                                      @RequestParam(required = false) String month) {
        User user = userService.getCurrentUser(authentication);
        YearMonth selectedMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        return calendarService.month(user, selectedMonth);
    }

    @GetMapping("/recommendations")
    public Map<String, Object> recommendations(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        MenstrualPhase phase = cycleService.currentPrediction(user)
                .map(CyclePrediction::getPhase)
                .orElse(MenstrualPhase.FOLLICULAR);
        return Map.of(
                "phase", phase,
                "nutrition", recommendationService.nutritionForPhase(phase),
                "exercises", recommendationService.exercisesForPhase(phase)
        );
    }

    @PostMapping("/water")
    public ResponseEntity<Map<String, Object>> logWater(Authentication authentication,
                                                       @Valid @RequestBody WaterLogDto dto) {
        User user = userService.getCurrentUser(authentication);
        waterService.save(user, dto);
        return ResponseEntity.ok(Map.of("waterToday", waterService.totalForToday(user)));
    }
}
