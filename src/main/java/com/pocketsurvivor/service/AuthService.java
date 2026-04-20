package com.pocketsurvivor.service;

import com.pocketsurvivor.config.JwtUtil;
import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.exception.EmailNotFoundException;
import com.pocketsurvivor.exception.IncorrectPasswordException;
import com.pocketsurvivor.model.PasswordResetToken;
import com.pocketsurvivor.model.Streak;
import com.pocketsurvivor.model.User;
import com.pocketsurvivor.repository.PasswordResetTokenRepository;
import com.pocketsurvivor.repository.StreakRepository;
import com.pocketsurvivor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserRepository userRepo;
    private final StreakRepository streakRepo;
    private final PasswordResetTokenRepository resetTokenRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("This email is already registered. Try logging in instead.");
        }

        User user = User.builder()
            .email(req.email())
            .passwordHash(passwordEncoder.encode(req.password()))
            .name(req.name())
            .personality(req.personality())
            .monthlyBudget(req.monthlyBudget())
            .build();
        user = userRepo.save(user);

        // Initialize streak record
        Streak streak = Streak.builder()
            .userId(user.getId())
            .currentStreak(0)
            .bestStreak(0)
            .build();
        streakRepo.save(streak);

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toResponse(user));
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepo.findByEmail(req.email())
            .orElseThrow(() -> new EmailNotFoundException("No account found with this email. Please register first."));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IncorrectPasswordException("Incorrect password. Please try again.");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toResponse(user));
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest req) {
        var userOpt = userRepo.findByEmail(req.email());
        if (userOpt.isEmpty()) {
            // Don't reveal whether the email exists
            return;
        }

        User user = userOpt.get();
        String otp = String.format("%06d", new SecureRandom().nextInt(1_000_000));

        PasswordResetToken resetToken = PasswordResetToken.builder()
            .userId(user.getId())
            .token(otp)
            .expiresAt(OffsetDateTime.now().plusMinutes(15))
            .used(false)
            .build();
        resetTokenRepo.save(resetToken);

        log.info("Password reset OTP for {}: {}", req.email(), otp);
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest req) {
        User user = userRepo.findByEmail(req.email())
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset code."));

        PasswordResetToken resetToken = resetTokenRepo
            .findTopByUserIdAndTokenOrderByIdDesc(user.getId(), req.token())
            .orElseThrow(() -> new IllegalArgumentException("Invalid or expired reset code."));

        if (resetToken.getUsed()) {
            throw new IllegalArgumentException("This reset code has already been used. Please request a new one.");
        }

        if (resetToken.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new IllegalArgumentException("Invalid or expired reset code.");
        }

        resetToken.setUsed(true);
        resetTokenRepo.save(resetToken);

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepo.save(user);
    }

    public UserResponse getProfile(Long userId) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, UserUpdateRequest req) {
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (req.name() != null) user.setName(req.name());
        if (req.personality() != null) user.setPersonality(req.personality());
        if (req.monthlyBudget() != null) user.setMonthlyBudget(req.monthlyBudget());

        return toResponse(userRepo.save(user));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getEmail(), u.getName(),
            u.getPersonality(), u.getMonthlyBudget(), u.getOnboardedAt());
    }
}
