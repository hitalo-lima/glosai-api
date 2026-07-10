package com.hitalo.glosai.exception;

import java.time.Instant;

public record ErrorResponse(
        Instant timestamp,
        int status,
        String mensagem
) {
}
