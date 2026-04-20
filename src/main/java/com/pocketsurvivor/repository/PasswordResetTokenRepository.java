package com.pocketsurvivor.repository;

import com.pocketsurvivor.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findTopByUserIdAndTokenOrderByIdDesc(Long userId, String token);
}
