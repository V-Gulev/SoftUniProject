package com.fittrack.mainapp.repository;

import com.fittrack.mainapp.model.entity.User;
import com.fittrack.mainapp.model.entity.WorkoutPlan;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class WorkoutPlanRepositoryTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private WorkoutPlanRepository workoutPlanRepository;

    @Test
    void testFindByUser_ShouldReturnPlansForGivenUser() {
        User user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@test.com");
        user1.setPassword("pass");
        testEntityManager.persist(user1);

        WorkoutPlan plan1 = new WorkoutPlan();
        plan1.setName("Plan for User 1");
        plan1.setUser(user1);
        testEntityManager.persist(plan1);

        List<WorkoutPlan> plans = workoutPlanRepository.findByUser(user1);
        assertEquals(1, plans.size());
        assertEquals("Plan for User 1", plans.get(0).getName());
    }
}
