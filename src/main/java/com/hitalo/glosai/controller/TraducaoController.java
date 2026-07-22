package com.hitalo.glosai.controller;

import com.hitalo.glosai.dto.TraducaoRequest;
import com.hitalo.glosai.dto.TraducaoResponse;
import com.hitalo.glosai.service.TraducaoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/traducao")
public class TraducaoController implements TraducaoApiDocs {

    private final TraducaoService traducaoService;

    public TraducaoController(TraducaoService traducaoService) {
        this.traducaoService = traducaoService;
    }

    @Override
    public ResponseEntity<TraducaoResponse> traduzir(@Valid @RequestBody TraducaoRequest request) {
        String glosa = traducaoService.traduzir(request.texto());
        return ResponseEntity.ok(new TraducaoResponse(glosa));
    }
}
