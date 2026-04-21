package com.pocketsurvivor.service;

import com.pocketsurvivor.dto.Dto.SmartSuggestionsResponse;
import com.pocketsurvivor.model.Expense.TimeOfDay;
import com.pocketsurvivor.model.LearningData;
import com.pocketsurvivor.model.LearningPrice;
import com.pocketsurvivor.repository.LearningDataRepository;
import com.pocketsurvivor.repository.LearningPriceRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LearningService {

    private final LearningDataRepository learningRepo;
    private final LearningPriceRepository priceRepo;
    private final CustomCategoryService customCategoryService;

    // Default categories per time slot
    private static final Map<String, List<String>> WEEKDAY_CATS = Map.of(
        "morning",   List.of("Breakfast","Coffee","Auto","Bus","Snacks","Juice","Stationery"),
        "afternoon", List.of("Lunch","Canteen","Juice","Stationery","Print","Coffee","Snacks"),
        "evening",   List.of("Snacks","Cafe","Ride","Hangout","Gym","Tea","Groceries"),
        "night",     List.of("Dinner","Dessert","Online Order","Groceries","Snacks","Medicine")
    );

    private static final Map<String, List<String>> WEEKEND_CATS = Map.of(
        "morning",   List.of("Breakfast","Coffee","Brunch","Snacks","Juice","Gym"),
        "afternoon", List.of("Lunch","Cafe","Movie","Shopping","Hangout","Juice"),
        "evening",   List.of("Cafe","Movie","Hangout","Shopping","Ride","Snacks","Dinner"),
        "night",     List.of("Dinner","Dessert","Online Order","Hangout","Movie","Shopping")
    );

    private static final List<Integer> DEFAULT_PRICES = List.of(20, 40, 60, 80, 100, 150, 200);

    /**
     * Record an expense for NLP learning: update category frequency + price frequency.
     */
    @Transactional
    public void recordExpense(Long userId, TimeOfDay timeOfDay, String category, int amount) {
        // Update category frequency
        var existing = learningRepo.findByUserIdAndTimeOfDayAndCategory(userId, timeOfDay, category);
        if (existing.isPresent()) {
            existing.get().setFrequency(existing.get().getFrequency() + 1);
            learningRepo.save(existing.get());
        } else {
            learningRepo.save(LearningData.builder()
                .userId(userId).timeOfDay(timeOfDay).category(category).frequency(1).build());
        }

        // Update price frequency
        var existingPrice = priceRepo.findByUserIdAndCategoryAndAmount(userId, category, amount);
        if (existingPrice.isPresent()) {
            existingPrice.get().setFrequency(existingPrice.get().getFrequency() + 1);
            priceRepo.save(existingPrice.get());
        } else {
            priceRepo.save(LearningPrice.builder()
                .userId(userId).category(category).amount(amount).frequency(1).build());
        }
    }

    /**
     * Get smart category suggestions sorted by user frequency.
     * Merges base categories with learned frequencies.
     */
    public List<String> getSmartCategories(Long userId, TimeOfDay timeOfDay) {
        boolean weekend = isWeekend();
        Map<String, List<String>> source = weekend ? WEEKEND_CATS : WEEKDAY_CATS;
        List<String> base = source.getOrDefault(timeOfDay.name(), List.of());

        // Get learned frequencies
        List<LearningData> learned = learningRepo
            .findByUserIdAndTimeOfDayOrderByFrequencyDesc(userId, timeOfDay);
        Map<String, Integer> freqMap = learned.stream()
            .collect(Collectors.toMap(LearningData::getCategory, LearningData::getFrequency));

        // Sort base categories by frequency (most used first)
        List<String> sorted = new ArrayList<>(base);
        sorted.sort((a, b) -> freqMap.getOrDefault(b, 0) - freqMap.getOrDefault(a, 0));
        return sorted;
    }

    /**
     * Get smart price suggestions for a category, sorted by user frequency.
     */
    public List<Integer> getSmartPrices(Long userId, String category) {
        List<LearningPrice> learned = priceRepo
            .findByUserIdAndCategoryOrderByFrequencyDesc(userId, category);

        if (!learned.isEmpty() && learned.get(0).getFrequency() >= 2) {
            int topPrice = learned.get(0).getAmount();
            List<Integer> result = new ArrayList<>();
            result.add(topPrice);
            for (int p : DEFAULT_PRICES) {
                if (p != topPrice) result.add(p);
            }
            return result;
        }
        return DEFAULT_PRICES;
    }

    /**
     * Full smart suggestions response for the bubble UI.
     * Merges default categories with user's custom categories.
     */
    public SmartSuggestionsResponse getSuggestions(Long userId, TimeOfDay timeOfDay) {
        List<String> categories = getSmartCategories(userId, timeOfDay);

        List<String> customNames = customCategoryService.getUserCategoryNames(userId, timeOfDay);
        Set<String> existing = new HashSet<>(categories);
        for (String name : customNames) {
            if (existing.add(name)) {
                categories.add(name);
            }
        }

        Map<String, List<Integer>> prices = new LinkedHashMap<>();
        for (String cat : categories) {
            prices.put(cat, getSmartPrices(userId, cat));
        }
        return new SmartSuggestionsResponse(categories, prices);
    }

    private boolean isWeekend() {
        DayOfWeek day = LocalDate.now().getDayOfWeek();
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }
}
