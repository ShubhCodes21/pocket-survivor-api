package com.pocketsurvivor.repository;

import com.pocketsurvivor.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByUserIdAndExpenseDateOrderByCreatedAtDesc(Long userId, LocalDate date);

    List<Expense> findByUserIdAndExpenseDateBetweenOrderByExpenseDateDescCreatedAtDesc(
        Long userId, LocalDate start, LocalDate end);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.userId = :userId AND e.expenseDate = :date")
    Integer sumByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM Expense e " +
           "WHERE e.userId = :userId AND e.expenseDate BETWEEN :start AND :end")
    Integer sumByUserIdAndDateBetween(@Param("userId") Long userId,
                                      @Param("start") LocalDate start,
                                      @Param("end") LocalDate end);

    @Query("SELECT e.category, SUM(e.amount), COUNT(e) FROM Expense e " +
           "WHERE e.userId = :userId AND e.expenseDate BETWEEN :start AND :end " +
           "GROUP BY e.category ORDER BY SUM(e.amount) DESC")
    List<Object[]> getCategoryBreakdown(@Param("userId") Long userId,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);

    @Query("SELECT e.expenseDate, SUM(e.amount) FROM Expense e " +
           "WHERE e.userId = :userId AND e.expenseDate BETWEEN :start AND :end " +
           "GROUP BY e.expenseDate ORDER BY e.expenseDate")
    List<Object[]> getDailyTotals(@Param("userId") Long userId,
                                   @Param("start") LocalDate start,
                                   @Param("end") LocalDate end);

    void deleteByIdAndUserId(Long id, Long userId);
}
