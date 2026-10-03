package br.com.projeto.util;

/** Exceção com mensagem amigável, pronta para ser exibida ao usuário final. */
public class AppException extends RuntimeException {

    public AppException(String message) {
        super(message);
    }

    public AppException(String message, Throwable cause) {
        super(message, cause);
    }
}
