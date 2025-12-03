package com.fittrack.mainapp.controller;

import com.fittrack.mainapp.service.LlmService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Controller
@RequestMapping("/api/llm")
public class LlmController {


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
        String message = payload.get("message");
        String response = llmService.chat(message);
        return Map.of("response", response);
    }
}
