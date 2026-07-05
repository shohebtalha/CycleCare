package com.cyclecare.controller;

import com.cyclecare.domain.ClotSize;
import com.cyclecare.domain.FlowColor;
import com.cyclecare.domain.FlowLevel;
import com.cyclecare.domain.User;
import com.cyclecare.dto.CycleDto;
import com.cyclecare.dto.FlowDto;
import com.cyclecare.service.CycleService;
import com.cyclecare.service.FlowNutritionRecommendationService;
import com.cyclecare.service.FlowService;
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
public class CycleController {

    private final UserService userService;
    private final CycleService cycleService;
    private final FlowService flowService;
    private final FlowNutritionRecommendationService flowNutritionRecommendationService;

    public CycleController(UserService userService,
                           CycleService cycleService,
                           FlowService flowService,
                           FlowNutritionRecommendationService flowNutritionRecommendationService) {
        this.userService = userService;
        this.cycleService = cycleService;
        this.flowService = flowService;
        this.flowNutritionRecommendationService = flowNutritionRecommendationService;
    }

    @GetMapping("/cycles")
    public String cycles(Authentication authentication, Model model) {
        User user = userService.getCurrentUser(authentication);
        model.addAttribute("cycleDto", new CycleDto());
        populateFlowModel(user, model, new FlowDto());
        model.addAttribute("cycles", cycleService.allCycles(user));
        model.addAttribute("prediction", cycleService.currentPrediction(user).orElse(null));
        return "cycles";
    }

    @PostMapping("/cycles")
    public String saveCycle(Authentication authentication,
                            @Valid @ModelAttribute CycleDto cycleDto,
                            BindingResult bindingResult,
                            Model model,
                            RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        if (bindingResult.hasErrors()) {
            populateFlowModel(user, model, new FlowDto());
            model.addAttribute("cycles", cycleService.allCycles(user));
            model.addAttribute("prediction", cycleService.currentPrediction(user).orElse(null));
            return "cycles";
        }
        cycleService.saveCycle(user, cycleDto);
        redirectAttributes.addFlashAttribute("success", "Cycle details saved.");
        return "redirect:/cycles";
    }

    @PostMapping("/cycles/flow")
    public String saveFlow(Authentication authentication,
                           @Valid @ModelAttribute FlowDto flowDto,
                           BindingResult bindingResult,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        User user = userService.getCurrentUser(authentication);
        if (bindingResult.hasErrors()) {
            model.addAttribute("cycleDto", new CycleDto());
            populateFlowModel(user, model, flowDto);
            model.addAttribute("cycles", cycleService.allCycles(user));
            model.addAttribute("prediction", cycleService.currentPrediction(user).orElse(null));
            return "cycles";
        }
        try {
            flowService.save(user, flowDto);
            redirectAttributes.addFlashAttribute("success", "Blood flow details saved.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/cycles";
    }

    private void populateFlowModel(User user, Model model, FlowDto flowDto) {
        model.addAttribute("flowDto", flowDto);
        model.addAttribute("flowEntries", flowService.history(user));
        model.addAttribute("flowLevels", FlowLevel.values());
        model.addAttribute("flowColors", FlowColor.values());
        model.addAttribute("clotSizes", ClotSize.values());
        model.addAttribute("flowRecommendation",
                flowService.latest(user)
                        .map(flowNutritionRecommendationService::forEntry)
                        .orElse(null));
    }
}
