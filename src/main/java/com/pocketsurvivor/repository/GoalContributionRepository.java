package com.pocketsurvivor.repository;

import com.pocketsurvivor.model.GoalContribution;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface GoalContributionRepository extends JpaRepository<GoalContribution, Long> {
    List<GoalContribution> findByGoalIdOrderByContributedAtDesc(Long goalId);
}
