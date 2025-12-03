package com.fittrack.mainapp.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LlmServiceImplTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient.Builder chatClientBuilder;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChatClient chatClient;

    private LlmServiceImpl llmService;

    @BeforeEach
    void setUp() {
        when(chatClientBuilder.build()).thenReturn(chatClient);
        llmService = new LlmServiceImpl(chatClientBuilder);
    }

    @Test
    void chat_ShouldReturnResponse_WhenCallSucceeds() {
        String userMessage = "Hello";
        String expectedResponse = "Hi there!";

        when(chatClient.prompt().system(anyString()).user(userMessage).call().content()).thenReturn(expectedResponse);

        String actualResponse = llmService.chat(userMessage);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void chat_ShouldReturnQuotaError_WhenQuotaExceeded() {
        String userMessage = "Expensive Request";
        String exceptionMessage = "Error: insufficient_quota for this key";
        String expectedResponse = "I'm sorry, the OpenAI API quota has been exceeded. Please check your billing details.";

        when(chatClient.prompt().system(anyString()).user(userMessage).call().content())
                .thenThrow(new RuntimeException(exceptionMessage));

        String actualResponse = llmService.chat(userMessage);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void chat_ShouldReturnRateLimitError_When429Occurs() {
        String userMessage = "Too Fast";
        String exceptionMessage = "Server returned HTTP response code: 429";
        String expectedResponse = "I'm sorry, I'm receiving too many requests right now. Please try again later.";

        when(chatClient.prompt().system(anyString()).user(userMessage).call().content())
                .thenThrow(new RuntimeException(exceptionMessage));

        String actualResponse = llmService.chat(userMessage);

        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void chat_ShouldReturnGenericError_WhenUnknownExceptionOccurs() {
        String userMessage = "Crash";
        String exceptionMessage = "Random database error";
        String expectedResponse = "I'm sorry, I encountered an error processing your request.";

        when(chatClient.prompt().system(anyString()).user(userMessage).call().content())
                .thenThrow(new RuntimeException(exceptionMessage));

        String actualResponse = llmService.chat(userMessage);

        assertEquals(expectedResponse, actualResponse);
    }
}
