package com.hitalo.glosai.service;

import com.hitalo.glosai.dto.GroqApiCallRequest;
import com.hitalo.glosai.dto.GroqApiCallRequest.Message;
import com.hitalo.glosai.dto.GroqApiCallResponse;
import com.hitalo.glosai.exception.ForaDeEscopoException;
import com.hitalo.glosai.exception.RespostaTruncadaException;
import com.hitalo.glosai.util.TextoNormalizador;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Service
public class TraducaoService {

    private static final Logger log = LoggerFactory.getLogger(TraducaoService.class);

    private final RestClient restClient;
    private final LogTranslationService logTranslationService;
    private final Cache cache;
    private final String apiKey;
    private final String model;
    private final double temperature;
    private final int maxTokens;

    public TraducaoService(RestClient restClient,
            LogTranslationService logTranslationService,
            CacheManager cacheManager,
            @Value("${groq.api.key}") String apiKey,
            @Value("${groq.api.model}") String model,
            @Value("${groq.api.temperature}") double temperature,
            @Value("${groq.api.max-tokens}") int maxTokens) {
        this.restClient = restClient;
        this.logTranslationService = logTranslationService;
        this.cache = cacheManager.getCache("traducoes");
        this.apiKey = apiKey;
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
    }

    public String traduzir(String texto) {
        String chave = TextoNormalizador.normalizar(texto);

        try {
            if (cache != null) {
                String cached = cache.get(chave, String.class);
                if (cached != null) {
                    log.info("Cache HIT para: {}", texto);
                    logTranslationService.salvarLog(texto, cached, true, true);
                    return cached;
                }
            }

            log.info("Cache miss - chamando Groq API para: {}", texto);
            String glosa = chamarGroq(texto);
            log.info("Traducao realizada com sucesso: {}", glosa);

            if ("ERRO_ESCOPO".equals(glosa)) {
                logTranslationService.salvarLog(texto, null, false, false);
                throw new ForaDeEscopoException();
            }

            if (cache != null) {
                cache.put(chave, glosa);
            }

            logTranslationService.salvarLog(texto, glosa, true, false);
            return glosa;

        } catch (ForaDeEscopoException e) {
            throw e;
        } catch (Exception e) {
            logTranslationService.salvarLog(texto, null, false, false);
            throw e;
        }
    }

    private String chamarGroq(String texto) {
        GroqApiCallRequest requestBody = new GroqApiCallRequest(
                model,
                List.of(
                        Message.system(systemPrompt()),
                        Message.user(texto)),
                temperature,
                maxTokens);

        try {
            GroqApiCallResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + apiKey)
                    .body(requestBody)
                    .retrieve()
                    .body(GroqApiCallResponse.class);

            return extrairResposta(response);

        } catch (RestClientException ex) {
            log.error("Falha na chamada a Groq API: {}", ex.getMessage(), ex);
            throw new RuntimeException("Falha na comunicação com o serviço de tradução.", ex);
        }
    }

    private String extrairResposta(GroqApiCallResponse response) {
        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new RuntimeException("Resposta inválida ou vazia do serviço de tradução.");
        }

        if ("length".equals(response.choices().getFirst().finishReason())) {
            throw new RespostaTruncadaException(
                    "A resposta do serviço de tradução foi cortada devido ao limite de tokens.");
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
                Você traduz português para GLOSA (notação textual da Língua Brasileira de Sinais).

                REGRA MAIS IMPORTANTE - FIDELIDADE:
                Use APENAS palavras e ideias que estão explicitamente no texto original.
                NUNCA adicione palavra, pronome, tempo ou partícula interrogativa que não
                esteja no texto. Se não sabe o que fazer com um trecho, mantenha o mais
                próximo possível do original em vez de inventar.

                PRONOMES: mantenha EU, VOCÊ, ELE, ELA, MEU, DELA, NOSSO exatamente como
                estão no original. Nunca troque um pelo outro.

                ORDEM E ESTRUTURA:
                - Marcador de tempo/lugar no início (AMANHÃ, HOJE, ONTEM, AQUI).
                - Sem artigos (o, a, um, uma) e sem preposições (em, para, de, por, com).
                - Verbos no infinitivo ou forma neutra, sem conjugação.
                - Negação (NÃO) sempre IMEDIATAMENTE APÓS o verbo, nunca antes.
                - Quando a frase tiver dois verbos em sequência (ex: "pode ajudar a encontrar",
                "quero aprender a nadar"), preserve AMBOS os verbos na glosa - nunca descarte
                um deles.

                PERGUNTAS: escolha a partícula certa, nunca use POR QUE como padrão genérico.
                - Lugar → ONDE | Tempo → QUANDO | Quantidade/preço → QUANTO
                - Pessoa → QUEM | Motivo → POR QUE
                - Se a pergunta é do tipo sim/não (sem palavra interrogativa no original,
                ex: "Tudo certo?"), NÃO invente uma partícula - apenas reordene o conteúdo
                - Se a frase for uma pergunta, adicione a partícula "(PERGUNTA)" no final da glosa.
      
                FORMATO: tudo em CAIXA ALTA. Retorne APENAS a glosa, sem explicações.

                ERRO_ESCOPO: use somente se o texto for símbolos/código, sem sentido algum,
                ou em outro idioma. Expressões coloquiais e informais em português (gírias,
                frases do dia a dia) são válidas e DEVEM ser traduzidas normalmente, nunca
                rejeitadas.

                EXEMPLOS CORRETOS:

                PT: "Oi, tudo bem?"
                GLOSA: OI VOCÊ TUDO BEM (PERGUNTA)

                PT: "Eu não gosto de café"
                GLOSA: CAFÉ EU GOSTAR NÃO

                PT: "Onde você mora?"
                GLOSA: VOCÊ MORAR ONDE (PERGUNTA)

                PT: "Você pode me ajudar a encontrar a farmácia mais próxima?"
                GLOSA: FARMÁCIA PERTO VOCÊ AJUDAR PODER EU ENCONTRAR (PERGUNTA)

                PT: "Quando é a reunião?"
                GLOSA: REUNIÃO QUANDO (PERGUNTA)

                PT: "Quanto custa isso?"
                GLOSA: ISSO CUSTAR QUANTO (PERGUNTA)

                PT: "Vou dar uma escapada rapidinho"
                GLOSA: RAPIDINHO EU ESCAPAR

                PT: "Meu carro quebrou ontem"
                GLOSA: ONTEM CARRO MEU QUEBRAR

                PT: "Isso não tem nada a ver"
                GLOSA: ISSO NADA VER NÃO

                PT: "Minha filha não quer comer legumes no jantar"
                GLOSA: JANTAR LEGUME FILHA MINHA COMER QUERER NÃO

                EXEMPLOS DE ERRO_ESCOPO (rejeitar):

                PT: "123 @ _ asdaskflgjf"
                GLOSA: ERRO_ESCOPO

                PT: "print('Hello, World!')"
                GLOSA: ERRO_ESCOPO

                PT: "Hello, how are you?"
                GLOSA: ERRO_ESCOPO
                """;
    }
}
