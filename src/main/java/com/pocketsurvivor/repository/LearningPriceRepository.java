package com.pocketsurvivor.repository;

import com.pocketsurvivor.model.LearningPrice;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface LearningPriceRepository extends JpaRepository<LearningPrice, Long> {
    List<LearningPrice> findByUserIdAndCategoryOrderByFrequencyDesc(Long userId, String category);
    Optional<LearningPrice> findByUserIdAndCategoryAndAmount(Long userId, String category, Integer amount);
}
