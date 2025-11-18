package com.fittrack.badgeservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.badgeservice.model.dto.BadgeAwardDto;
import com.fittrack.badgeservice.service.BadgeService;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BadgeController.class)
class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Mock
    private BadgeService mockBadgeService;

    @Test
    void testAwardBadge_ShouldReturnCreatedStatus() throws Exception {
        BadgeAwardDto awardDto = new BadgeAwardDto("Test Badge", "url", UUID.randomUUID());

        mockMvc.perform(post("/badges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(awardDto)))
                .andExpect(status().isCreated());
    }
}