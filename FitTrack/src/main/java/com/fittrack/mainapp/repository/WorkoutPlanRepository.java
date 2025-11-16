package com.fittrack.mainapp.repository;

import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.entity.WorkoutPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutPlanRepository extends JpaRepository<WorkoutPlan, UUID> {

    List<WorkoutPlan> findByUser(User user);

    Optional<Object> findByIdAndUser(UUID planId, User user);

}