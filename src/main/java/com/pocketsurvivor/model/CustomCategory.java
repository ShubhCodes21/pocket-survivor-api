package com.pocketsurvivor.model;

import com.pocketsurvivor.model.Expense.TimeOfDay;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "custom_categories", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "name"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class CustomCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    @Builder.Default
    private String icon = "Receipt";

    @Enumerated(EnumType.STRING)
    @Column(name = "time_of_day", length = 20)
    private TimeOfDay timeOfDay;

    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = OffsetDateTime.now();
        if (icon == null) icon = "Receipt";
    }
}
