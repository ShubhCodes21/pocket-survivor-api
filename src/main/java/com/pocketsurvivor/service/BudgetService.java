package com.pocketsurvivor.service;

import com.pocketsurvivor.model.User;
import com.pocketsurvivor.repository.ExpenseRepository;
import com.pocketsurvivor.repository.GoalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final ExpenseRepository expenseRepo;
    private final GoalRepository goalRepo;

    /**
     * Core formula:
     * Daily Budget = (Monthly Budget - Spent So Far - Goal Requirements) / Days Left
     */
    public int calcDailyBudget(User user) {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        int daysLeft = YearMonth.now().lengthOfMonth() - today.getDayOfMonth() + 1;

        int monthSpent = expenseRepo.sumByUserIdAndDateBetween(user.getId(), monthStart, today);
        int goalReq = goalRepo.findByUserIdOrderByCreatedAtDesc(user.getId()).stream()
            .filter(g -> !g.getIsCompleted())
            .mapToInt(g -> Math.max(0, g.getTargetAmount() - g.getSavedAmount()))
            .sum();

        return Math.max(0, (user.getMonthlyBudget() - monthSpent - goalReq) / Math.max(1, daysLeft));
    }

    public int getTodaySpent(Long userId) {
        return expenseRepo.sumByUserIdAndDate(userId, LocalDate.now());
    }

    public int getWeekSpent(Long userId) {
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() % 7);
        return expenseRepo.sumByUserIdAndDateBetween(userId, weekStart, today);
    }

    public int getMonthSpent(Long userId) {
        LocalDate today = LocalDate.now();
        return expenseRepo.sumByUserIdAndDateBetween(userId, today.withDayOfMonth(1), today);
    }

    public int getDaysLeft() {
        LocalDate today = LocalDate.now();
        return YearMonth.now().lengthOfMonth() - today.getDayOfMonth() + 1;
    }

    /**
     * Spend status based on personality tolerance.
     * spender: 1.3x tolerance
     * balanced: 1.1x tolerance
     * saver: 0.9x tolerance
     */
    public String getSpendStatus(User user) {
        int daily = calcDailyBudget(user);
        int todaySpent = getTodaySpent(user.getId());
        double tolerance = switch (user.getPersonality()) {
            case spender -> 1.3;
            case balanced -> 1.1;
            case saver -> 0.9;
        };

        if (todaySpent > daily * tolerance) return "over";
        if (todaySpent <= daily * 0.8) return "under";
        return "on_track";
    }
}
