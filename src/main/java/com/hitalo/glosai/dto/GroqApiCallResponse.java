package com.hitalo.glosai.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GroqApiCallResponse(
        List<Choice> choices
) {
    public record Choice(
            Message message,
            @JsonProperty("finish_reason")
            String finishReason
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }
}
