package com.hitalo.glosai.dto;

import java.util.List;

public record GroqApiCallResponse(
        List<Choice> choices
) {
    public record Choice(
            Message message
    ) {
    }

    public record Message(
            String role,
            String content
    ) {
    }
}
