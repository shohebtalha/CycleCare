package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.dto.JournalEntryDto;
import com.cyclecare.service.JournalService;
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
public class JournalController {

    private final UserService userService;
    private final JournalService journalService;

    public JournalController(UserService userService, JournalService journalService) {
        this.userService = userService;
        this.journalService = journalService;
    }

    @GetMapping("/journal")
    public String journal(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        populate(model, user, new JournalEntryDto());
        return "journal";
    }

    @PostMapping("/journal")
    public String saveJournal(Authentication authentication,
                              @Valid @ModelAttribute JournalEntryDto journalEntryDto,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        if (bindingResult.hasErrors()) {
            populate(model, user, journalEntryDto);
            return "journal";
        }
        journalService.save(user, journalEntryDto);
        redirectAttributes.addFlashAttribute("success", "Journal entry saved.");
        return "redirect:/journal";
    }

    private void populate(Model model, User user, JournalEntryDto dto) {
        model.addAttribute("journalEntryDto", dto);
        model.addAttribute("entries", journalService.history(user));
    }
}
