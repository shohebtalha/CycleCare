package com.cyclecare.controller;

import com.cyclecare.domain.MenstrualPhase;
import com.cyclecare.domain.User;
import com.cyclecare.dto.AssistantQuestionDto;
import com.cyclecare.service.AssistantService;
import com.cyclecare.service.CyclePrediction;
import com.cyclecare.service.CycleService;
import com.cyclecare.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AssistantController {

    private final UserService userService;
    private final CycleService cycleService;
    private final AssistantService assistantService;

    public AssistantController(UserService userService,
                               CycleService cycleService,
                               AssistantService assistantService) {
        this.userService = userService;
        this.cycleService = cycleService;
        this.assistantService = assistantService;
    }

    @GetMapping("/assistant")
    public String assistant(Model model) {
        model.addAttribute("assistantQuestionDto", new AssistantQuestionDto());
        return "assistant";
    }

    @PostMapping("/assistant")
    public String ask(Authentication authentication,
                      @Valid @ModelAttribute AssistantQuestionDto assistantQuestionDto,
                      BindingResult bindingResult,
                      Model model) {
        if (bindingResult.hasErrors()) {
            return "assistant";
        }
        User user = userService.getCurrentUser(authentication);
        MenstrualPhase phase = cycleService.currentPrediction(user)
                .map(CyclePrediction::getPhase)
                .orElse(MenstrualPhase.FOLLICULAR);
        assistantQuestionDto.setAnswer(assistantService.answer(assistantQuestionDto.getQuestion(), phase));
        model.addAttribute("assistantQuestionDto", assistantQuestionDto);
        return "assistant";
    }
}
