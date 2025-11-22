package com.fittrack.badgeservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.badgeservice.model.dto.BadgeAwardDto;
import com.fittrack.badgeservice.model.dto.BadgeDto;
import com.fittrack.badgeservice.service.BadgeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BadgeController.class)
class BadgeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BadgeService mockBadgeService;

    @Test
    void testAwardBadge_ShouldReturnCreatedStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        BadgeAwardDto awardDto = new BadgeAwardDto("Test Badge", "url", userId);
        BadgeDto returnedBadge = new BadgeDto(UUID.randomUUID(), "Test Badge", "url", userId);

        when(mockBadgeService.awardBadge(any(BadgeAwardDto.class))).thenReturn(returnedBadge);

        mockMvc.perform(post("/badges")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(awardDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Badge"));
    }

    @Test
    void testGetBadgesForUser_ShouldReturnOkStatus() throws Exception {
        UUID userId = UUID.randomUUID();
        List<BadgeDto> badges = Arrays.asList(
                new BadgeDto(UUID.randomUUID(), "Badge 1", "url1", userId),
                new BadgeDto(UUID.randomUUID(), "Badge 2", "url2", userId));

        when(mockBadgeService.getBadgesForUser(userId)).thenReturn(badges);

        mockMvc.perform(get("/badges/user/" + userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Badge 1"));
    }

    @Test
    void testDeleteBadge_ShouldReturnNoContentStatus() throws Exception {
        UUID badgeId = UUID.randomUUID();

        mockMvc.perform(delete("/badges/" + badgeId))
                .andExpect(status().isNoContent());

        verify(mockBadgeService).deleteBadge(badgeId);
    }
}