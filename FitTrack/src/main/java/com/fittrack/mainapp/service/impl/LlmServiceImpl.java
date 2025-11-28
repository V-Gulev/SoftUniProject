package com.fittrack.mainapp.service.impl;

import com.fittrack.mainapp.service.LlmService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class LlmServiceImpl implements LlmService {

    private final ChatClient chatClient;

    public LlmServiceImpl(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    @Override
    public String chat(String message) {
        return chatClient.prompt()
                .system("You are a helpful fitness assistant. Please keep your responses concise and to the point.")
                .user(message)
                .call()
                .content();
    }
}
