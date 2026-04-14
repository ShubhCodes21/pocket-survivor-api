package com.pocketsurvivor.service;

import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.model.Expense;
import com.pocketsurvivor.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepo;
    private final LearningService learningService;
    private final GamificationService gamificationService;

    @Transactional
    public ExpenseResponse addExpense(Long userId, ExpenseRequest req) {
        Expense expense = Expense.builder()
            .userId(userId)
            .category(req.category())
            .amount(req.amount())
            .timeOfDay(req.timeOfDay())
            .expenseDate(req.expenseDate() != null ? req.expenseDate() : LocalDate.now())
            .note(req.note())
            .build();
        expense = expenseRepo.save(expense);

        // Update NLP learning data
        learningService.recordExpense(userId, req.timeOfDay(), req.category(), req.amount());

        // Update streaks and badges
        gamificationService.onExpenseLogged(userId, expense.getExpenseDate());

        return toResponse(expense);
    }

    public List<ExpenseResponse> getTodayExpenses(Long userId) {
        return expenseRepo.findByUserIdAndExpenseDateOrderByCreatedAtDesc(userId, LocalDate.now())
            .stream().map(this::toResponse).toList();
    }

    public List<ExpenseResponse> getExpenses(Long userId, LocalDate from, LocalDate to) {
        return expenseRepo.findByUserIdAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(userId, from, to)
            .stream().map(this::toResponse).toList();
    }

    public List<CategoryBreakdown> getCategoryBreakdown(Long userId, LocalDate from, LocalDate to) {
        return expenseRepo.getCategoryBreakdown(userId, from, to).stream()
            .map(row -> new CategoryBreakdown(
                (String) row[0],
                ((Number) row[1]).intValue(),
                (Long) row[2]
            )).toList();
    }

    public List<DailyTotal> getDailyTotals(Long userId, LocalDate from, LocalDate to) {
        return expenseRepo.getDailyTotals(userId, from, to).stream()
            .map(row -> new DailyTotal(
                (LocalDate) row[0],
                ((Number) row[1]).intValue()
            )).toList();
    }

    @Transactional
    public void deleteExpense(Long userId, Long expenseId) {
        expenseRepo.deleteByIdAndUserId(expenseId, userId);
    }

    private ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getCategory(), e.getAmount(),
            e.getTimeOfDay(), e.getExpenseDate(), e.getNote());
    }
}
