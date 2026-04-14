package com.pocketsurvivor.service;

import com.pocketsurvivor.config.JwtUtil;
import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.model.Streak;
import com.pocketsurvivor.model.User;
import com.pocketsurvivor.repository.StreakRepository;
import com.pocketsurvivor.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepo;
    private final StreakRepository streakRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (userRepo.existsByEmail(req.email())) {
            throw new IllegalArgumentException("Email already registered");
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
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getEmail());
        return new AuthResponse(token, toResponse(user));
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
