package com.pocketsurvivor.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "learning_data",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "time_of_day", "category"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LearningData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "time_of_day", nullable = false, length = 20)
    private Expense.TimeOfDay timeOfDay;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(nullable = false)
    private Integer frequency;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        updatedAt = OffsetDateTime.now();
        if (frequency == null) frequency = 1;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = OffsetDateTime.now();
    }
}
