package com.pocketsurvivor.repository;

import com.pocketsurvivor.model.LearningData;
import com.pocketsurvivor.model.Expense.TimeOfDay;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LearningDataRepository extends JpaRepository<LearningData, Long> {
    List<LearningData> findByUserIdAndTimeOfDayOrderByFrequencyDesc(Long userId, TimeOfDay timeOfDay);
    Optional<LearningData> findByUserIdAndTimeOfDayAndCategory(Long userId, TimeOfDay timeOfDay, String category);
}
