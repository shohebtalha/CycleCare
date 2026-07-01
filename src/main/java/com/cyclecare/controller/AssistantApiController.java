package com.cyclecare.controller;

import com.cyclecare.domain.User;
import com.cyclecare.dto.ChatMessage;
import com.cyclecare.service.AssistantService;
import com.cyclecare.service.CyclePrediction;
import com.cyclecare.service.CycleService;
import com.cyclecare.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/assistant")
public class AssistantApiController {

    private final UserService userService;
    private final CycleService cycleService;
    private final AssistantService assistantService;

    public AssistantApiController(UserService userService,
                                  CycleService cycleService,
                                  AssistantService assistantService) {

        this.userService = userService;
        this.cycleService = cycleService;
        this.assistantService = assistantService;
    }

    @PostMapping("/clear")
    public void clearChat(HttpSession session) {

        session.removeAttribute("chatHistory");

    }

    @PostMapping
    public Map<String, String> ask(@RequestBody Map<String, String> body,
                                   Authentication authentication,
                                   HttpSession session) {

        String question = body.get("question");

        User user = userService.getCurrentUser(authentication);

        CyclePrediction prediction =
                cycleService.currentPrediction(user).orElse(null);

        @SuppressWarnings("unchecked")
        List<ChatMessage> history =
                (List<ChatMessage>) session.getAttribute("chatHistory");

        if(history == null){
            history = new ArrayList<>();
        }

        history.add(new ChatMessage("user",question));

        String answer =
                assistantService.answer(
                        question,
                        user,
                        prediction,
                        history
                );

        history.add(new ChatMessage("assistant",answer));

        session.setAttribute("chatHistory",history);

        return Map.of("answer",answer);
    }
}