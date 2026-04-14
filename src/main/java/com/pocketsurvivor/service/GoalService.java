package com.pocketsurvivor.service;

import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.model.Goal;
import com.pocketsurvivor.model.GoalContribution;
import com.pocketsurvivor.repository.GoalContributionRepository;
import com.pocketsurvivor.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepo;
    private final GoalContributionRepository contribRepo;
    private final GamificationService gamificationService;

    @Transactional
    public GoalResponse createGoal(Long userId, GoalRequest req) {
        Goal goal = Goal.builder()
            .userId(userId)
            .name(req.name())
            .targetAmount(req.targetAmount())
            .savedAmount(0)
            .deadline(req.deadline())
            .isCompleted(false)
            .build();
        goal = goalRepo.save(goal);
        gamificationService.onGoalCreated(userId);
        return toResponse(goal);
    }

    public List<GoalResponse> getGoals(Long userId) {
        return goalRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .map(this::toResponse).toList();
    }

    public List<GoalResponse> getActiveGoals(Long userId) {
        return goalRepo.findByUserIdOrderByCreatedAtDesc(userId).stream()
            .filter(g -> !g.getIsCompleted())
            .map(this::toResponse).toList();
    }

    @Transactional
    public GoalResponse addContribution(Long userId, Long goalId, ContributionRequest req) {
        Goal goal = goalRepo.findByIdAndUserId(goalId, userId)
            .orElseThrow(() -> new IllegalArgumentException("Goal not found"));

        int newSaved = Math.min(goal.getTargetAmount(), goal.getSavedAmount() + req.amount());
        goal.setSavedAmount(newSaved);
        if (newSaved >= goal.getTargetAmount()) {
            goal.setIsCompleted(true);
            gamificationService.onGoalCompleted(userId);
        }
        goal = goalRepo.save(goal);

        // Log the contribution
        GoalContribution contrib = GoalContribution.builder()
            .goalId(goalId)
            .userId(userId)
            .amount(req.amount())
            .build();
        contribRepo.save(contrib);

        return toResponse(goal);
    }

    @Transactional
    public void deleteGoal(Long userId, Long goalId) {
        goalRepo.deleteByIdAndUserId(goalId, userId);
    }

    private GoalResponse toResponse(Goal g) {
        Integer daysToDeadline = null;
        Integer dailySaveNeeded = null;

        if (g.getDeadline() != null && !g.getIsCompleted()) {
            daysToDeadline = (int) Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), g.getDeadline()));
            int remaining = g.getTargetAmount() - g.getSavedAmount();
            dailySaveNeeded = daysToDeadline > 0 ? (int) Math.ceil((double) remaining / daysToDeadline) : remaining;
        }

        return new GoalResponse(
            g.getId(), g.getName(), g.getTargetAmount(), g.getSavedAmount(),
            g.getDeadline(), g.getIsCompleted(), dailySaveNeeded, daysToDeadline
        );
    }
}
