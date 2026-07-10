package com.hitalo.glosai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TraducaoRequest(
        @NotBlank(message = "O campo 'texto' é obrigatório e não pode estar vazio.")
        @Size(max = 255, message = "O campo 'texto' deve ter no máximo 255 caracteres.")
        String texto
) {
}
