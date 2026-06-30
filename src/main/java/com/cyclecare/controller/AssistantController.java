package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.dto.AssistantQuestionDto;
import com.cyclecare.dto.ChatMessage;
import com.cyclecare.service.AssistantService;
import com.cyclecare.service.CyclePrediction;
import com.cyclecare.service.CycleService;
import com.cyclecare.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.ArrayList;
import java.util.List;

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
    public String assistant(Model model,
                            HttpSession session) {

        model.addAttribute("assistantQuestionDto",
                new AssistantQuestionDto());

        @SuppressWarnings("unchecked")
        List<ChatMessage> chatHistory =
                (List<ChatMessage>) session.getAttribute("chatHistory");

        if (chatHistory == null) {
            chatHistory = new ArrayList<>();
        }

        model.addAttribute("chatHistory", chatHistory);

        return "assistant";
    }

    @PostMapping("/assistant")
    public String ask(Authentication authentication,
                      @Valid @ModelAttribute AssistantQuestionDto assistantQuestionDto,
                      BindingResult bindingResult,
                      Model model,
                      HttpSession session) {
        if (bindingResult.hasErrors()) {
            return "assistant";
        }
        User user = userService.getCurrentUser(authentication);

        CyclePrediction prediction = cycleService.currentPrediction(user)
                .orElse(null);
        @SuppressWarnings("unchecked")
        List<ChatMessage> chatHistory =
                (List<ChatMessage>) session.getAttribute("chatHistory");

        if (chatHistory == null) {
            chatHistory = new ArrayList<>();
        }

        chatHistory.add(
                new ChatMessage(
                        "user",
                        assistantQuestionDto.getQuestion()
                )
        );
        String aiResponse = assistantService.answer(
                assistantQuestionDto.getQuestion(),
                user,
                prediction,
                chatHistory
        );

        assistantQuestionDto.setAnswer(aiResponse);

        chatHistory.add(
                new ChatMessage(
                        "assistant",
                        aiResponse
                )
        );

        session.setAttribute("chatHistory", chatHistory);

// Create a fresh DTO so the textarea is empty
        model.addAttribute("assistantQuestionDto", new AssistantQuestionDto());
        model.addAttribute("chatHistory", chatHistory);

        return "assistant";
    }
}
