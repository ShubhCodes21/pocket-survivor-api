package com.pocketsurvivor.service;

import com.pocketsurvivor.model.Badge;
import com.pocketsurvivor.model.Streak;
import com.pocketsurvivor.repository.BadgeRepository;
import com.pocketsurvivor.repository.ExpenseRepository;
import com.pocketsurvivor.repository.GoalRepository;
import com.pocketsurvivor.repository.StreakRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GamificationService {

    private final StreakRepository streakRepo;
    private final BadgeRepository badgeRepo;
    private final ExpenseRepository expenseRepo;
    private final GoalRepository goalRepo;

    @Transactional
    public void onExpenseLogged(Long userId, LocalDate expenseDate) {
        Streak streak = streakRepo.findByUserId(userId)
            .orElseGet(() -> {
                Streak s = Streak.builder()
                    .userId(userId).currentStreak(0).bestStreak(0).build();
                return streakRepo.save(s);
            });

        if (streak.getLastLogDate() == null) {
            streak.setCurrentStreak(1);
            streak.setBestStreak(1);
        } else {
            long daysDiff = ChronoUnit.DAYS.between(streak.getLastLogDate(), expenseDate);
            if (daysDiff == 0) {
                // Same day, no streak change
            } else if (daysDiff == 1) {
                streak.setCurrentStreak(streak.getCurrentStreak() + 1);
            } else {
                streak.setCurrentStreak(1);
            }
            streak.setBestStreak(Math.max(streak.getBestStreak(), streak.getCurrentStreak()));
        }
        streak.setLastLogDate(expenseDate);
        streakRepo.save(streak);

        // Check badges
        awardBadge(userId, "first_log");
        if (streak.getCurrentStreak() >= 3) awardBadge(userId, "streak_3");
        if (streak.getCurrentStreak() >= 7) awardBadge(userId, "streak_7");

        // Under budget badge
        int todaySpent = expenseRepo.sumByUserIdAndDate(userId, LocalDate.now());
        // Note: dailyBudget check happens at controller level since it needs User context
    }

    @Transactional
    public void onGoalCreated(Long userId) {
        awardBadge(userId, "goal_set");
    }

    @Transactional
    public void onGoalCompleted(Long userId) {
        awardBadge(userId, "goal_done");
    }

    @Transactional
    public void checkUnderBudgetBadge(Long userId, int todaySpent, int dailyBudget) {
        if (todaySpent > 0 && todaySpent <= dailyBudget) {
            awardBadge(userId, "under_budget");
        }
    }

    public List<String> getBadges(Long userId) {
        return badgeRepo.findByUserId(userId).stream()
            .map(Badge::getBadgeKey).toList();
    }

    public Streak getStreak(Long userId) {
        return streakRepo.findByUserId(userId)
            .orElse(Streak.builder().currentStreak(0).bestStreak(0).build());
    }

    private void awardBadge(Long userId, String badgeKey) {
        if (!badgeRepo.existsByUserIdAndBadgeKey(userId, badgeKey)) {
            badgeRepo.save(Badge.builder().userId(userId).badgeKey(badgeKey).build());
        }
    }
}
