package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class LlmServiceImpl implements LlmService {

    private static final Logger LOGGER = LoggerFactory.getLogger(LlmServiceImpl.class);
    private final ChatClient chatClient;

    public LlmServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String chat(String message) {
        try {
            LOGGER.info("Received chat message: {}", message);
            String response = chatClient.prompt()
                    .system("You are a helpful fitness assistant. Please keep your responses concise and to the point.")
                    .user(message)
                    .call()
                    .content();
            LOGGER.info("Generated response: {}", response);
            return response;
        } catch (Exception e) {
            LOGGER.error("Error processing chat message", e);
            return getErrorMessage(e);
        }
    }

    private String getErrorMessage(Exception e) {
        String errorMessage = "I'm sorry, I encountered an error processing your request.";
        if (e.getMessage() != null && e.getMessage().contains("insufficient_quota")) {
            errorMessage = "I'm sorry, the OpenAI API quota has been exceeded. Please check your billing details.";
        } else if (e.getMessage() != null && e.getMessage().contains("429")) {
            errorMessage = "I'm sorry, I'm receiving too many requests right now. Please try again later.";
        }
        return errorMessage;
    }
}
