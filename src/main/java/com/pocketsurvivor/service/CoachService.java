package com.pocketsurvivor.service;

import com.pocketsurvivor.model.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import java.time.Duration;
import java.time.YearMonth;
import java.time.LocalDate;
import java.util.*;

@Service
@Slf4j
public class CoachService {

    private final WebClient webClient;
    private final BudgetService budgetService;
    private final GoalService goalService;
    private final String model;

    // Personality-specific fallback messages
    private static final Map<String, Map<String, List<String>>> FALLBACKS = Map.of(
        "spender", Map.of(
            "over", List.of(
                "Bro you're BLEEDING money today. Put the wallet down.",
                "Your wallet called. It's filing for divorce.",
                "At this rate you're funding someone's startup with snack money."
            ),
            "on_track", List.of(
                "Not bad for a spender. Don't ruin it.",
                "Okay you're somehow within budget. Screenshot this."
            ),
            "under", List.of(
                "Wait... you're UNDER budget? Who are you?",
                "A spender saving money? The simulation is glitching."
            )
        ),
        "balanced", Map.of(
            "over", List.of(
                "You're over today's plan. Might want to ease up.",
                "Slightly above target. Tomorrow's a fresh start."
            ),
            "on_track", List.of(
                "Right on track. Keep this rhythm.",
                "Solid spending today. Nothing to worry about."
            ),
            "under", List.of(
                "Under budget — that extra goes to your goals.",
                "Clean day. Future you says thanks."
            )
        ),
        "saver", Map.of(
            "over", List.of(
                "A little over, but you've been great lately. No stress.",
                "Tiny bump — your savings are still amazing."
            ),
            "on_track", List.of(
                "You're doing beautifully. Every rupee saved is a win.",
                "Perfect pace! Goals are getting closer."
            ),
            "under", List.of(
                "Look at you go! Under budget AND saving. Champion!",
                "Your discipline is inspiring. That goal is practically yours!"
            )
        )
    );

    public CoachService(
        @Value("${app.anthropic.api-key}") String apiKey,
        @Value("${app.anthropic.model}") String model,
        BudgetService budgetService,
        GoalService goalService
    ) {
        this.model = model;
        this.budgetService = budgetService;
        this.goalService = goalService;
        this.webClient = WebClient.builder()
            .baseUrl("https://api.anthropic.com")
            .defaultHeader("x-api-key", apiKey)
            .defaultHeader("anthropic-version", "2023-06-01")
            .defaultHeader("Content-Type", "application/json")
            .build();
    }

    public String getCoachMessage(User user) {
        int todaySpent = budgetService.getTodaySpent(user.getId());
        int dailyBudget = budgetService.calcDailyBudget(user);
        int monthSpent = budgetService.getMonthSpent(user.getId());
        String status = budgetService.getSpendStatus(user);
        int daysLeft = budgetService.getDaysLeft();
        var activeGoals = goalService.getActiveGoals(user.getId());

        String toneDesc = switch (user.getPersonality()) {
            case spender -> "savage, blunt, roast-style, Gen-Z slang. Roast if overspending. Use Indian college slang.";
            case balanced -> "practical, matter-of-fact, concise. Be direct and honest.";
            case saver -> "warm, encouraging, celebratory. Celebrate wins warmly.";
        };

        String goalsStr = activeGoals.isEmpty() ? "none"
            : activeGoals.stream()
                .map(g -> g.name() + "(₹" + g.savedAmount() + "/₹" + g.targetAmount() + ")")
                .reduce((a, b) -> a + ", " + b).orElse("none");

        String prompt = String.format("""
            You are a spending coach for an Indian college day-scholar named %s. Personality: "%s".

            Monthly pocket money: ₹%d | Spent this month: ₹%d | Today's budget: ₹%d | Spent today: ₹%d (%s) | Days left: %d
            Active goals: %s

            TONE: %s

            ONE coaching message, max 2 sentences. Be specific with numbers. Reference goals if relevant. Sound like a college friend. No emojis. No quotes around the message.
            """,
            user.getName(), user.getPersonality().name(),
            user.getMonthlyBudget(), monthSpent, dailyBudget, todaySpent,
            status.equals("over") ? "OVER budget" : "on track/under",
            daysLeft, goalsStr, toneDesc
        );

        try {
            Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 200,
                "messages", List.of(Map.of("role", "user", "content", prompt))
            );

            String response = webClient.post()
                .uri("/v1/messages")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .timeout(Duration.ofSeconds(10))
                .map(resp -> {
                    var content = (List<Map<String, Object>>) resp.get("content");
                    if (content != null && !content.isEmpty()) {
                        return (String) content.get(0).get("text");
                    }
                    return null;
                })
                .onErrorResume(e -> {
                    log.warn("Anthropic API call failed: {}", e.getMessage());
                    return Mono.empty();
                })
                .block();

            if (response != null && response.length() > 5) {
                return response;
            }
        } catch (Exception e) {
            log.warn("Coach message generation failed, using fallback: {}", e.getMessage());
        }

        return getFallbackMessage(user.getPersonality().name(), status);
    }

    private String getFallbackMessage(String personality, String status) {
        String statusKey = switch (status) {
            case "over" -> "over";
            case "under" -> "under";
            default -> "on_track";
        };
        List<String> messages = FALLBACKS
            .getOrDefault(personality, FALLBACKS.get("balanced"))
            .getOrDefault(statusKey, List.of("Keep going!"));
        return messages.get(new Random().nextInt(messages.size()));
    }
}
