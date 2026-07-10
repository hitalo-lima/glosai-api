package com.hitalo.glosai.controller;

import com.hitalo.glosai.dto.TraducaoRequest;
import com.hitalo.glosai.dto.TraducaoResponse;
import com.hitalo.glosai.service.TraducaoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traducao")
@Tag(name = "Tradução", description = "Endpoint de tradução PT → GLOSA")
public class TraducaoController {

    private final TraducaoService traducaoService;

    public TraducaoController(TraducaoService traducaoService) {
        this.traducaoService = traducaoService;
    }

    @PostMapping
    @Operation(
            summary = "Traduzir texto em português para GLOSA",
            description = "Recebe uma frase em português e retorna a representação em GLOSA (Libras textual).",
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = TraducaoRequest.class),
                            examples = @ExampleObject(
                                    name = "Exemplo de requisição",
                                    value = "{\"texto\": \"Você pode me ajudar a encontrar a farmácia mais próxima?\"}"
                            )
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tradução realizada com sucesso",
                            content = @Content(schema = @Schema(implementation = TraducaoResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Texto nulo, vazio ou com mais de 255 caracteres"),
                    @ApiResponse(responseCode = "422", description = "Texto fora do escopo de tradução"),
                    @ApiResponse(responseCode = "500", description = "Erro interno na chamada à Groq API")
            }
    )
    public ResponseEntity<TraducaoResponse> traduzir(@Valid @org.springframework.web.bind.annotation.RequestBody TraducaoRequest request) {
        String glosa = traducaoService.traduzir(request.texto());
        return ResponseEntity.ok(new TraducaoResponse(glosa));
    }
}
