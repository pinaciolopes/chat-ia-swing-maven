package br.com.projeto.config;

import br.com.projeto.util.AppException;

public final class AppConfig {

    private AppConfig() {
    }

    public static String require(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new AppException("Configuração ausente: defina a variável de ambiente " + name);
        }
        return value;
    }
}
