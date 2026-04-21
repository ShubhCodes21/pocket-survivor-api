package com.pocketsurvivor.repository;

import com.pocketsurvivor.model.CustomCategory;
import com.pocketsurvivor.model.Expense.TimeOfDay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CustomCategoryRepository extends JpaRepository<CustomCategory, Long> {

    List<CustomCategory> findByUserIdOrderByCreatedAtAsc(Long userId);

    List<CustomCategory> findByUserIdAndTimeOfDayIsNullOrUserIdAndTimeOfDay(
        Long userId1, Long userId2, TimeOfDay timeOfDay);

    boolean existsByUserIdAndName(Long userId, String name);

    void deleteByIdAndUserId(Long id, Long userId);
}
