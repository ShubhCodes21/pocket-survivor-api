package com.pocketsurvivor.dto;

import com.pocketsurvivor.model.Expense.TimeOfDay;
import com.pocketsurvivor.model.User.Personality;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class Dto {
    private Dto() {}

    // ── AUTH ──────────────────────────────────────────────────────
    public record LoginRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password
    ) {}

    public record RegisterRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = 6) String password,
        @NotBlank @Size(max = 100) String name,
        @NotNull Personality personality,
        @NotNull @Min(5000) @Max(50000) Integer monthlyBudget
    ) {}

    public record AuthResponse(String token, UserResponse user) {}

    public record ForgotPasswordRequest(
        @NotBlank @Email String email
    ) {}

    public record ResetPasswordRequest(
        @NotBlank @Email String email,
        @NotBlank String token,
        @NotBlank @Size(min = 6) String newPassword
    ) {}

    // ── USER ─────────────────────────────────────────────────────
    public record UserResponse(
        Long id, String email, String name,
        Personality personality, Integer monthlyBudget, LocalDate onboardedAt
    ) {}

    public record UserUpdateRequest(
        @Size(max = 100) String name,
        Personality personality,
        @Min(5000) @Max(50000) Integer monthlyBudget
    ) {}

    // ── EXPENSE ──────────────────────────────────────────────────
    public record ExpenseRequest(
        @NotBlank @Size(max = 50) String category,
        @NotNull @Min(1) Integer amount,
        @NotNull TimeOfDay timeOfDay,
        LocalDate expenseDate,
        @Size(max = 255) String note
    ) {}

    public record ExpenseResponse(
        Long id, String category, Integer amount,
        TimeOfDay timeOfDay, LocalDate expenseDate, String note
    ) {}

    // ── GOAL ─────────────────────────────────────────────────────
    public record GoalRequest(
        @NotBlank @Size(max = 100) String name,
        @NotNull @Min(1) Integer targetAmount,
        LocalDate deadline
    ) {}

    public record GoalResponse(
        Long id, String name, Integer targetAmount, Integer savedAmount,
        LocalDate deadline, Boolean isCompleted,
        Integer dailySaveNeeded, Integer daysToDeadline
    ) {}

    public record ContributionRequest(
        @NotNull @Min(1) Integer amount
    ) {}

    // ── DASHBOARD ────────────────────────────────────────────────
    public record DashboardResponse(
        Integer todaySpent, Integer weekSpent, Integer monthSpent,
        Integer dailyBudget, Integer monthlyBudget, Integer remaining,
        Integer daysLeft, String spendStatus, String coachMessage,
        List<ExpenseResponse> todayExpenses, List<GoalResponse> activeGoals,
        StreakResponse streak
    ) {}

    // ── INSIGHTS ─────────────────────────────────────────────────
    public record InsightsResponse(
        Integer avgDaily, String topCategory, Integer topCategoryAmount,
        List<CategoryBreakdown> categoryBreakdown,
        List<DailyTotal> dailyTotals,
        StreakResponse streak, List<String> badges
    ) {}

    public record CategoryBreakdown(String category, Integer totalSpent, Long count) {}
    public record DailyTotal(LocalDate date, Integer amount) {}

    // ── STREAK / BADGE ───────────────────────────────────────────
    public record StreakResponse(Integer currentStreak, Integer bestStreak, LocalDate lastLogDate) {}

    // ── LEARNING / NLP ───────────────────────────────────────────
    public record SmartSuggestionsResponse(
        List<String> categories,
        Map<String, List<Integer>> prices
    ) {}

    // ── CUSTOM CATEGORIES ────────────────────────────────────────
    public record CustomCategoryRequest(
        @NotBlank @Size(max = 50) String name,
        @Size(max = 30) String icon,
        TimeOfDay timeOfDay
    ) {}

    public record CustomCategoryResponse(
        Long id, String name, String icon, TimeOfDay timeOfDay
    ) {}

    // ── RECURRING EXPENSES ───────────────────────────────────────
    public record RecurringExpenseRequest(
        @NotBlank @Size(max = 50) String category,
        @NotNull @Min(1) Integer amount,
        @NotNull TimeOfDay timeOfDay,
        @Size(max = 255) String note,
        @Size(max = 20) String daysOfWeek
    ) {}

    public record RecurringExpenseResponse(
        Long id, String category, Integer amount, TimeOfDay timeOfDay,
        String note, Boolean isActive, String daysOfWeek, LocalDate lastAutoLogged
    ) {}

    // ── GENERIC ──────────────────────────────────────────────────
    public record ApiResponse<T>(boolean success, String message, T data) {
        public static <T> ApiResponse<T> ok(T data) {
            return new ApiResponse<>(true, "OK", data);
        }
        public static <T> ApiResponse<T> ok(String msg, T data) {
            return new ApiResponse<>(true, msg, data);
        }
        public static <T> ApiResponse<T> error(String msg) {
            return new ApiResponse<>(false, msg, null);
        }
    }
}
