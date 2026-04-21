package com.pocketsurvivor.repository;

import com.pocketsurvivor.model.RecurringExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface RecurringExpenseRepository extends JpaRepository<RecurringExpense, Long> {

    List<RecurringExpense> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<RecurringExpense> findByIsActiveTrueAndLastAutoLoggedIsNullOrIsActiveTrueAndLastAutoLoggedBefore(
        LocalDate date);

    List<RecurringExpense> findByUserIdAndIsActiveTrue(Long userId);

    void deleteByIdAndUserId(Long id, Long userId);
}
