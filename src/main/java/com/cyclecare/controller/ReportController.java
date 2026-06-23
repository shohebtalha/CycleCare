package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.service.CycleService;
import com.cyclecare.service.JournalService;
import com.cyclecare.service.MoodService;
import com.cyclecare.service.ReportService;
import com.cyclecare.service.SleepService;
import com.cyclecare.service.SymptomService;
import com.cyclecare.service.UserService;
import com.cyclecare.service.WaterService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReportController {

    private final UserService userService;
    private final CycleService cycleService;
    private final SymptomService symptomService;
    private final MoodService moodService;
    private final WaterService waterService;
    private final SleepService sleepService;
    private final JournalService journalService;
    private final ReportService reportService;

    public ReportController(UserService userService,
                            CycleService cycleService,
                            SymptomService symptomService,
                            MoodService moodService,
                            WaterService waterService,
                            SleepService sleepService,
                            JournalService journalService,
                            ReportService reportService) {
        this.userService = userService;
        this.cycleService = cycleService;
        this.symptomService = symptomService;
        this.moodService = moodService;
        this.waterService = waterService;
        this.sleepService = sleepService;
        this.journalService = journalService;
        this.reportService = reportService;
    }

    @GetMapping("/reports")
    public String reports(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("cycles", cycleService.allCycles(user));
        model.addAttribute("symptoms", symptomService.history(user));
        model.addAttribute("moods", moodService.history(user));
        model.addAttribute("waterLogs", waterService.recent(user));
        model.addAttribute("sleepLogs", sleepService.recent(user));
        model.addAttribute("journalEntries", journalService.latest(user));
        return "reports";
    }

    @GetMapping("/reports/export")
    public ResponseEntity<byte[]> export(Authentication authentication) {
        User user = userService.getCurrentUser(authentication);
        byte[] pdf = reportService.generate(user);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("cyclecare-health-report.pdf")
                .build());
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
