package com.hitalo.glosai.service;

public interface LogTranslationService {

    void salvarLog(String input, String output, boolean success, boolean isCached);
}
