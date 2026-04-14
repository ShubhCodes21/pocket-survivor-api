package com.pocketsurvivor.controller;

import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.service.GoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class GoalController {

    private final GoalService goalService;

    @PostMapping
    public ResponseEntity<ApiResponse<GoalResponse>> createGoal(
        Authentication auth,
        @Valid @RequestBody GoalRequest req
    ) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Goal created", goalService.createGoal(userId, req)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GoalResponse>>> getGoals(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(goalService.getGoals(userId)));
    }

    @PostMapping("/{id}/contribute")
    public ResponseEntity<ApiResponse<GoalResponse>> addContribution(
        Authentication auth,
        @PathVariable Long id,
        @Valid @RequestBody ContributionRequest req
    ) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok("Savings added", goalService.addContribution(userId, id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGoal(
        Authentication auth,
        @PathVariable Long id
    ) {
        Long userId = (Long) auth.getPrincipal();
        goalService.deleteGoal(userId, id);
        return ResponseEntity.ok(ApiResponse.ok("Goal deleted", null));
    }
}
