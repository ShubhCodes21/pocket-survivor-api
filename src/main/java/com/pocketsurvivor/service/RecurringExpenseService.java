package com.pocketsurvivor.service;

import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.model.Expense;
import com.pocketsurvivor.model.RecurringExpense;
import com.pocketsurvivor.repository.ExpenseRepository;
import com.pocketsurvivor.repository.RecurringExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RecurringExpenseService {

    private final RecurringExpenseRepository recurringRepo;
    private final ExpenseRepository expenseRepo;
    private final LearningService learningService;
    private final GamificationService gamificationService;

    public List<RecurringExpenseResponse> getUserRecurring(Long userId) {
        return recurringRepo.findByUserIdOrderByCreatedAtDesc(userId)
            .stream().map(this::toResponse).toList();
    }

    @Transactional
    public RecurringExpenseResponse create(Long userId, RecurringExpenseRequest req) {
        RecurringExpense re = RecurringExpense.builder()
            .userId(userId)
            .category(req.category())
            .amount(req.amount())
            .timeOfDay(req.timeOfDay())
            .note(req.note())
            .daysOfWeek(req.daysOfWeek() != null ? req.daysOfWeek() : "weekdays")
            .build();
        return toResponse(recurringRepo.save(re));
    }

    @Transactional
    public RecurringExpenseResponse toggle(Long userId, Long id) {
        RecurringExpense re = recurringRepo.findById(id)
            .filter(r -> r.getUserId().equals(userId))
            .orElseThrow(() -> new IllegalArgumentException("Recurring expense not found"));
        re.setIsActive(!re.getIsActive());
        return toResponse(recurringRepo.save(re));
    }

    @Transactional
    public void delete(Long userId, Long id) {
        recurringRepo.deleteByIdAndUserId(id, userId);
    }

    @Transactional
    public void processRecurringForUser(Long userId) {
        LocalDate today = LocalDate.now();
        DayOfWeek dow = today.getDayOfWeek();

        List<RecurringExpense> active = recurringRepo.findByUserIdAndIsActiveTrue(userId);

        for (RecurringExpense re : active) {
            if (today.equals(re.getLastAutoLogged())) continue;
            if (!matchesDay(re.getDaysOfWeek(), dow)) continue;

            Expense expense = Expense.builder()
                .userId(userId)
                .category(re.getCategory())
                .amount(re.getAmount())
                .timeOfDay(re.getTimeOfDay())
                .expenseDate(today)
                .note(re.getNote() != null ? re.getNote() + " (auto)" : "(auto)")
                .build();
            expenseRepo.save(expense);

            learningService.recordExpense(userId, re.getTimeOfDay(), re.getCategory(), re.getAmount());
            gamificationService.onExpenseLogged(userId, today);

            re.setLastAutoLogged(today);
            recurringRepo.save(re);
        }
    }

    private boolean matchesDay(String daysOfWeek, DayOfWeek dow) {
        if (daysOfWeek == null || daysOfWeek.isBlank()) return false;
        return switch (daysOfWeek.toLowerCase()) {
            case "daily" -> true;
            case "weekdays" -> dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY;
            case "weekends" -> dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
            default -> {
                Set<String> days = Set.of(daysOfWeek.toLowerCase().split(","));
                String abbr = dow.name().substring(0, 3).toLowerCase();
                yield days.contains(abbr);
            }
        };
    }

    private RecurringExpenseResponse toResponse(RecurringExpense re) {
        return new RecurringExpenseResponse(
            re.getId(), re.getCategory(), re.getAmount(), re.getTimeOfDay(),
            re.getNote(), re.getIsActive(), re.getDaysOfWeek(), re.getLastAutoLogged()
        );
    }
}
