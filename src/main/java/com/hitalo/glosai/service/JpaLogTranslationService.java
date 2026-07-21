package com.hitalo.glosai.service;

import com.hitalo.glosai.model.LogTranslation;
import com.hitalo.glosai.repository.LogTranslationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.persistence.enabled", havingValue = "true")
public class JpaLogTranslationService implements LogTranslationService {

    private static final Logger log = LoggerFactory.getLogger(JpaLogTranslationService.class);

    private final LogTranslationRepository repository;

    public JpaLogTranslationService(LogTranslationRepository repository) {
        this.repository = repository;
    }

    @Override
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
