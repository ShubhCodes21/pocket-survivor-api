package com.pocketsurvivor.service;

import com.pocketsurvivor.dto.Dto.*;
import com.pocketsurvivor.model.CustomCategory;
import com.pocketsurvivor.model.Expense.TimeOfDay;
import com.pocketsurvivor.repository.CustomCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomCategoryService {

    private final CustomCategoryRepository categoryRepo;

    public List<CustomCategoryResponse> getUserCategories(Long userId) {
        return categoryRepo.findByUserIdOrderByCreatedAtAsc(userId)
            .stream().map(this::toResponse).toList();
    }

    public List<String> getUserCategoryNames(Long userId, TimeOfDay timeOfDay) {
        return categoryRepo.findByUserIdOrderByCreatedAtAsc(userId).stream()
            .filter(c -> c.getTimeOfDay() == null || c.getTimeOfDay() == timeOfDay)
            .map(CustomCategory::getName)
            .toList();
    }

    @Transactional
    public CustomCategoryResponse createCategory(Long userId, CustomCategoryRequest req) {
        if (categoryRepo.existsByUserIdAndName(userId, req.name())) {
            throw new IllegalArgumentException("Category '" + req.name() + "' already exists");
        }
        CustomCategory cat = CustomCategory.builder()
            .userId(userId)
            .name(req.name())
            .icon(req.icon() != null ? req.icon() : "Receipt")
            .timeOfDay(req.timeOfDay())
            .build();
        return toResponse(categoryRepo.save(cat));
    }

    @Transactional
    public void deleteCategory(Long userId, Long id) {
        categoryRepo.deleteByIdAndUserId(id, userId);
    }

    private CustomCategoryResponse toResponse(CustomCategory c) {
        return new CustomCategoryResponse(c.getId(), c.getName(), c.getIcon(), c.getTimeOfDay());
    }
}
