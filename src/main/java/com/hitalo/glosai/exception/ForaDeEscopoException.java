package com.hitalo.glosai.exception;

public class ForaDeEscopoException extends RuntimeException {

    public ForaDeEscopoException() {
        super("O texto fornecido não pôde ser traduzido para GLOSA.");
    }
}
