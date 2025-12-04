package com.fittrack.mainapp.repository;

import com.fittrack.mainapp.model.entity.Goal;
import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.enums.GoalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoalRepository extends JpaRepository<Goal, UUID> {

    List<Goal> findByUser(User user);

    Optional<Goal> findByIdAndUser(UUID id, User user);

    List<Goal> findByStatusAndArchivedFalseAndCompletedDateBefore(GoalStatus status, LocalDateTime date);

    long countByCompletedDateAfter(LocalDateTime dateTime);

    long countByCompletedDateBetween(LocalDateTime start, LocalDateTime end);

}