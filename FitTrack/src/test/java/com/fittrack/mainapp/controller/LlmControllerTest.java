package com.fittrack.mainapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fittrack.mainapp.repository.UserRepository;
import com.fittrack.mainapp.service.LlmService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LlmController.class)
class LlmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LlmService llmService;

    @MockitoBean
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    void chatView_ShouldReturnChatTemplate() throws Exception {
        mockMvc.perform(get("/api/llm/view"))
                .andExpect(status().isOk())
                .andExpect(view().name("chat"));
    }

    @Test
    @WithMockUser
    void chat_ShouldReturnResponse_WhenServiceSucceeds() throws Exception {
        String userMessage = "Hello AI";
        String aiResponse = "Hello Human";
        Map<String, String> payload = Map.of("message", userMessage);

        when(llmService.chat(userMessage)).thenReturn(aiResponse);

        mockMvc.perform(post("/api/llm/chat")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(aiResponse));
    }

    @Test
    @WithMockUser
    void chat_ShouldReturnGenericError_WhenServiceReturnsError() throws Exception {
        Map<String, String> payload = Map.of("message", "Trigger Error");
        String errorMessage = "I'm sorry, I encountered an error processing your request.";

        when(llmService.chat(anyString())).thenReturn(errorMessage);

        mockMvc.perform(post("/api/llm/chat")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(errorMessage));
    }

    @Test
    @WithMockUser
    void chat_ShouldReturnQuotaError_WhenServiceReturnsQuotaError() throws Exception {
        Map<String, String> payload = Map.of("message", "Expensive Request");
        String errorMessage = "I'm sorry, the OpenAI API quota has been exceeded. Please check your billing details.";

        when(llmService.chat(anyString())).thenReturn(errorMessage);

        mockMvc.perform(post("/api/llm/chat")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(errorMessage));
    }

    @Test
    @WithMockUser
    void chat_ShouldReturnRateLimitError_WhenServiceReturnsRateLimitError() throws Exception {
        Map<String, String> payload = Map.of("message", "Too Fast");
        String errorMessage = "I'm sorry, I'm receiving too many requests right now. Please try again later.";

        when(llmService.chat(anyString())).thenReturn(errorMessage);

        mockMvc.perform(post("/api/llm/chat")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.response").value(errorMessage));
    }
}