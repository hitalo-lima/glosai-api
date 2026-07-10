package com.hitalo.glosai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record GroqApiCallRequest(
        String model,
        List<Message> messages,
        double temperature,
        @JsonProperty("max_tokens") int maxTokens
) {
    public record Message(String role, String content) {
    }
}
