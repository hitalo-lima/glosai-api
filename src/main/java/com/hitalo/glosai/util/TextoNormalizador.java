package com.hitalo.glosai.util;

public final class TextoNormalizador {

    private TextoNormalizador() {}

    /**
     * Normaliza o texto para ser usado como chave de cache.
     * Remove espaços extras, pontuação final e converte para minúsculas.
     *
     * @param texto O texto a ser normalizado.
     * @return O texto normalizado.
     */
    public static String normalizar(String texto) {
        if (texto == null) return null;
        return texto.trim()
                .toLowerCase()
                .replaceAll(" +", " ")
                .replaceAll("[!.,;]+$", "");
    }
}
