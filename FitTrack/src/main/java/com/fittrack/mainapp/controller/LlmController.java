package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.service.LlmService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/llm")
public class LlmController {

    private static final Logger logger = LoggerFactory.getLogger(LlmController.class);
    private final LlmService llmService;

    public LlmController(LlmService llmService) {
        this.llmService = llmService;
    }

    @GetMapping("/view")
    public String chatView() {
        return "chat";
    }

    @PostMapping("/chat")
    @ResponseBody
    public Map<String, String> chat(@RequestBody Map<String, String> payload) {
        try {
            String message = payload.get("message");
            logger.info("Received chat message: {}", message);
            String response = llmService.chat(message);
            logger.info("Generated response: {}", response);
            return Map.of("response", response);
        } catch (Exception e) {
            logger.error("Error processing chat message", e);
            String errorMessage = getErrorMessage(e);
            return Map.of("response", errorMessage);
        }
    }

    private static String getErrorMessage(Exception e) {
        String errorMessage = "I'm sorry, I encountered an error processing your request.";
        if (e.getMessage() != null && e.getMessage().contains("insufficient_quota")) {
            errorMessage = "I'm sorry, the OpenAI API quota has been exceeded. Please check your billing details.";
        } else if (e.getMessage() != null && e.getMessage().contains("429")) {
            errorMessage = "I'm sorry, I'm receiving too many requests right now. Please try again later.";
        }
        return errorMessage;
    }
}
