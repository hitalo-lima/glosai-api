package com.hitalo.glosai.service;

import com.hitalo.glosai.dto.GroqApiCallRequest;
import com.hitalo.glosai.dto.GroqApiCallRequest.Message;
import com.hitalo.glosai.dto.GroqApiCallResponse;
import com.hitalo.glosai.exception.ForaDeEscopoException;
import com.hitalo.glosai.exception.RespostaTruncadaException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class TraducaoService {

    private static final Logger log = LoggerFactory.getLogger(TraducaoService.class);

    private final RestClient restClient;
    private final String apiKey;
    private final String model;
    private final double temperature;
    private final int maxTokens;

    public TraducaoService(RestClient restClient,
                           @Value("${groq.api.key}") String apiKey,
                           @Value("${groq.api.model}") String model,
                           @Value("${groq.api.temperature}") double temperature,
                           @Value("${groq.api.max-tokens}") int maxTokens) {
        this.restClient = restClient;
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    public String traduzir(String texto) {
        GroqApiCallRequest requestBody = new GroqApiCallRequest(
                model,
                List.of(
                        new Message("system", systemPrompt()),
                        new Message("user", texto)
                ),
                temperature,
                maxTokens
        );

        try {
            GroqApiCallResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(GroqApiCallResponse.class);

            String glosa = extrairResposta(response);
            log.info("Tradução realizada com sucesso: {}", glosa);

            if ("ERRO_ESCOPO".equals(glosa)) {
                throw new ForaDeEscopoException();
            }

            return glosa;

        } catch (RestClientException ex) {
            log.error("Falha na chamada à Groq API: {}", ex.getMessage(), ex);
            throw new RuntimeException("Falha na comunicação com o serviço de tradução.", ex);
        }
    }

    private String extrairResposta(GroqApiCallResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new RuntimeException("Resposta inválida ou vazia do serviço de tradução.");
        }

        if ("length".equals(response.choices().getFirst().finishReason())) {
            throw new RespostaTruncadaException("A resposta do serviço de tradução foi cortada devido ao limite de tokens.");
        }

        String content = response.choices().getFirst().message().content();
        if (content == null) {
            throw new RuntimeException("Resposta incompleta do serviço de tradução.");
        }

        content = content.trim();

        if (content.isBlank()) {
            throw new RuntimeException("Resposta vazia do serviço de tradução.");
        }

        return content;
    }

    private String systemPrompt() {
        return """
                Você é um tradutor de português para GLOSA (representação textual da Língua Brasileira de Sinais).

                Regras obrigatórias:
                - Marcadores de tempo/lugar sempre no início.
                - Sem artigos (o, a, um, uma).
                - Sem preposições (em, para, de, por, com).
                - Sem conjugações verbais (infinitivo ou forma neutra).
                - Negação depois do verbo.
                - Perguntas: partícula interrogativa geralmente no final (POR QUE, QUANDO, ONDE).
                - Tudo em CAIXA ALTA.
                - Retornar APENAS a glosa, sem explicações.
                - Se o texto de entrada fugir do escopo, não for uma frase em português, for vazio de sentido, contiver apenas símbolos/código, ou qualquer conteúdo que não seja uma frase legítima para tradução, retorne exatamente a palavra ERRO_ESCOPO.

                Exemplos de casos corretos:
                PT: "Eu vou precisar ir ao banco amanhã de manhã para resolver um problema no meu cartão"
                GLOSA: AMANHÃ MANHÃ BANCO EU IR CARTÃO PROBLEMA RESOLVER

                PT: "Você pode me ajudar a encontrar a farmácia mais próxima?"
                GLOSA: FARMÁCIA PERTO AJUDAR VOCÊ PODER EU ENCONTRAR

                PT: "Minha filha não quer comer legumes no jantar"
                GLOSA: JANTAR LEGUME FILHA MINHA COMER QUERER NÃO

                Exemplo de casos incorretos:
                PT: "123 @ _ asdaskflgjf"
                GLOSA: ERRO_ESCOPO

                PT: "print('Hello, World!')"
                GLOSA: ERRO_ESCOPO

                PT: "Hello, how are you?"
                GLOSA: ERRO_ESCOPO
                """;
    }
}
