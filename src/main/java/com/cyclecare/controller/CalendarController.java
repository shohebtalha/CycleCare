package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.service.CalendarDay;
import com.cyclecare.service.CalendarService;
import com.cyclecare.service.UserService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.stream.IntStream;

@Controller
public class CalendarController {

    private final UserService userService;
    private final CalendarService calendarService;

    public CalendarController(UserService userService, CalendarService calendarService) {
        this.userService = userService;
        this.calendarService = calendarService;
    }

    @GetMapping("/calendar")
    public String calendar(Authentication authentication,
                           @RequestParam(required = false) String month,
                           Model model) {
        User user = userService.getCurrentUser(authentication);
        YearMonth selectedMonth = month == null || month.isBlank() ? YearMonth.now() : YearMonth.parse(month);
        model.addAttribute("selectedMonth", selectedMonth);
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("previousMonth", selectedMonth.minusMonths(1));
        model.addAttribute("nextMonth", selectedMonth.plusMonths(1));
        model.addAttribute("leadingBlanks",
                IntStream.range(1, selectedMonth.atDay(1).getDayOfWeek().getValue()).boxed().toList());
        var days = calendarService.month(user, selectedMonth);
        model.addAttribute("days", days);
        model.addAttribute("periodDays", days.stream().filter(day -> day.isPeriodDay() && !day.isPredictedPeriod()).count());
        model.addAttribute("predictedDays", days.stream().filter(day -> day.isPredictedPeriod()).count());
        model.addAttribute("symptomDays", days.stream().filter(day -> !day.getSymptoms().isEmpty()).count());
        model.addAttribute("flowDays", days.stream().filter(CalendarDay::isFlowLogged).count());
        return "calendar";
    }
}
