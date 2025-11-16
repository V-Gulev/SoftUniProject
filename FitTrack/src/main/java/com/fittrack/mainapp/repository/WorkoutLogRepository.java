package com.fittrack.mainapp.repository;

import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.entity.WorkoutLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WorkoutLogRepository extends JpaRepository<WorkoutLog, UUID> {

    List<WorkoutLog> findByUser(User user);

    List<WorkoutLog> findByUserOrderByDateDesc(User user);

    Optional<WorkoutLog> findByIdAndUser(UUID id, User user);

}