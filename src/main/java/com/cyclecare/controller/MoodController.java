package com.cyclecare.controller;

import com.cyclecare.domain.MoodType;
import com.cyclecare.domain.User;
import com.cyclecare.dto.MoodDto;
import com.cyclecare.service.MoodService;
import com.cyclecare.service.UserService;
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
public class MoodController {

    private final UserService userService;
    private final MoodService moodService;

    public MoodController(UserService userService, MoodService moodService) {
        this.userService = userService;
        this.moodService = moodService;
    }

    @GetMapping("/moods")
    public String moods(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        populate(model, user, new MoodDto());
        return "moods";
    }

    @PostMapping("/moods")
    public String saveMood(Authentication authentication,
                           @Valid @ModelAttribute MoodDto moodDto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        if (bindingResult.hasErrors()) {
            populate(model, user, moodDto);
            return "moods";
        }
        moodService.save(user, moodDto);
        redirectAttributes.addFlashAttribute("success", "Mood entry saved.");
        return "redirect:/moods";
    }

    private void populate(Model model, User user, MoodDto dto) {
        model.addAttribute("moodDto", dto);
        model.addAttribute("moodTypes", MoodType.values());
        model.addAttribute("moods", moodService.history(user));
    }
}
