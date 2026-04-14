package com.pocketsurvivor.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "badges", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "badge_key"}))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Badge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "badge_key", nullable = false, length = 50)
    private String badgeKey;

    @Column(name = "earned_at", nullable = false, updatable = false)
    private OffsetDateTime earnedAt;

    @PrePersist
    protected void onCreate() {
        earnedAt = OffsetDateTime.now();
    }
}
