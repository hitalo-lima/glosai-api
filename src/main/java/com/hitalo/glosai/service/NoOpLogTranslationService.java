package com.hitalo.glosai.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "app.persistence.enabled", havingValue = "false", matchIfMissing = true)
public class NoOpLogTranslationService implements LogTranslationService {

    private static final Logger log = LoggerFactory.getLogger(NoOpLogTranslationService.class);

    @Override
    public void salvarLog(String input, String output, boolean success, boolean isCached) {
        log.debug("Log de tradução desabilitado - persistência não configurada");
    }
}
