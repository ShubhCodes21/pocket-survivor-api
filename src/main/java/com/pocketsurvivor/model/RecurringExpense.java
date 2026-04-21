package com.pocketsurvivor.model;

import com.pocketsurvivor.model.Expense.TimeOfDay;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Entity
@Table(name = "recurring_expenses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class RecurringExpense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Integer amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_of_day", nullable = false, length = 20)
    private TimeOfDay timeOfDay;

    @Column(length = 255)
    private String note;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "days_of_week", nullable = false, length = 20)
    @Builder.Default
    private String daysOfWeek = "weekdays";

    @Column(name = "last_auto_logged")
    private LocalDate lastAutoLogged;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (isActive == null) isActive = true;
        if (daysOfWeek == null) daysOfWeek = "weekdays";
    }
}
