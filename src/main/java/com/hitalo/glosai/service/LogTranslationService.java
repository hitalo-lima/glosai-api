package com.hitalo.glosai.service;

import com.hitalo.glosai.model.LogTranslation;
import com.hitalo.glosai.repository.LogTranslationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogTranslationService {

    private static final Logger log = LoggerFactory.getLogger(LogTranslationService.class);

    private final LogTranslationRepository repository;

    public LogTranslationService(LogTranslationRepository repository) {
        this.repository = repository;
    }

    @Async
    public void salvarLog(String input, String output, boolean success, boolean isCached) {
        try {
            LogTranslation entity = new LogTranslation(input, output, success, isCached);
            repository.save(entity);
            log.debug("Log salvo: input={}, success={}, cached={}", input, success, isCached);
        } catch (Exception e) {
            log.error("Falha ao salvar log de traducao: {}", e.getMessage(), e);
        }
    }
}
