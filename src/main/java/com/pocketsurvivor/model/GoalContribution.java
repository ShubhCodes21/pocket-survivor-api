package com.pocketsurvivor.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "goal_contributions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class GoalContribution {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "goal_id", nullable = false)
    private Long goalId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "contributed_at", nullable = false, updatable = false)
    private OffsetDateTime contributedAt;

    @PrePersist
    protected void onCreate() {
        contributedAt = OffsetDateTime.now();
    }
}
