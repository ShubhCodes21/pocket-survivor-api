package com.pocketsurvivor.controller;

import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.model.Expense.TimeOfDay;
import com.pocketsurvivor.model.Streak;
import com.pocketsurvivor.model.User;
import com.pocketsurvivor.repository.UserRepository;
import com.pocketsurvivor.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepo;
    private final BudgetService budgetService;
    private final ExpenseService expenseService;
    private final GoalService goalService;
    private final CoachService coachService;
    private final GamificationService gamificationService;
    private final LearningService learningService;
    private final RecurringExpenseService recurringExpenseService;

    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId).orElseThrow();

        recurringExpenseService.processRecurringForUser(userId);

        int todaySpent = budgetService.getTodaySpent(userId);
        int weekSpent = budgetService.getWeekSpent(userId);
        int monthSpent = budgetService.getMonthSpent(userId);
        int dailyBudget = budgetService.calcDailyBudget(user);
        int remaining = user.getMonthlyBudget() - monthSpent;
        int daysLeft = budgetService.getDaysLeft();
        String status = budgetService.getSpendStatus(user);

        // Check under-budget badge
        gamificationService.checkUnderBudgetBadge(userId, todaySpent, dailyBudget);

        String coachMsg = coachService.getCoachMessage(user);
        List<ExpenseResponse> todayExps = expenseService.getTodayExpenses(userId);
        List<GoalResponse> activeGoals = goalService.getActiveGoals(userId);
        Streak streak = gamificationService.getStreak(userId);

        DashboardResponse dashboard = new DashboardResponse(
            todaySpent, weekSpent, monthSpent,
            dailyBudget, user.getMonthlyBudget(), Math.max(0, remaining),
            daysLeft, status, coachMsg,
            todayExps, activeGoals,
            new StreakResponse(streak.getCurrentStreak(), streak.getBestStreak(), streak.getLastLogDate())
        );

        return ResponseEntity.ok(ApiResponse.ok(dashboard));
    }

    @GetMapping("/insights")
    public ResponseEntity<ApiResponse<InsightsResponse>> getInsights(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate weekAgo = today.minusDays(6);

        int monthSpent = budgetService.getMonthSpent(userId);
        int avgDaily = today.getDayOfMonth() > 0 ? monthSpent / today.getDayOfMonth() : 0;

        List<CategoryBreakdown> catBreakdown = expenseService.getCategoryBreakdown(userId, monthStart, today);
        String topCat = catBreakdown.isEmpty() ? null : catBreakdown.get(0).category();
        Integer topCatAmt = catBreakdown.isEmpty() ? null : catBreakdown.get(0).totalSpent();

        List<DailyTotal> dailyTotals = expenseService.getDailyTotals(userId, weekAgo, today);
        List<String> badges = gamificationService.getBadges(userId);
        Streak streak = gamificationService.getStreak(userId);

        InsightsResponse insights = new InsightsResponse(
            avgDaily, topCat, topCatAmt,
            catBreakdown, dailyTotals,
            new StreakResponse(streak.getCurrentStreak(), streak.getBestStreak(), streak.getLastLogDate()),
            badges
        );

        return ResponseEntity.ok(ApiResponse.ok(insights));
    }

    @GetMapping("/coach")
    public ResponseEntity<ApiResponse<String>> getCoachMessage(Authentication auth) {
        Long userId = (Long) auth.getPrincipal();
        User user = userRepo.findById(userId).orElseThrow();
        return ResponseEntity.ok(ApiResponse.ok(coachService.getCoachMessage(user)));
    }

    @GetMapping("/suggestions/{timeOfDay}")
    public ResponseEntity<ApiResponse<SmartSuggestionsResponse>> getSuggestions(
        Authentication auth,
        @PathVariable TimeOfDay timeOfDay
    ) {
        Long userId = (Long) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(learningService.getSuggestions(userId, timeOfDay)));
    }
}
