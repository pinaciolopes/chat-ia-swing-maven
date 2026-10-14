package br.com.projeto.util;

import com.mongodb.MongoException;

/** Converte qualquer erro em uma mensagem compreensível para o usuário final. */
public final class ErrorHandler {

    private ErrorHandler() {
    }

    public static String paraUsuario(Throwable erro) {
        if (erro instanceof AppException) {
            return erro.getMessage();
        }

        // erro técnico: fica no console para o desenvolvedor, nunca na tela do usuário
        erro.printStackTrace();

        if (erro instanceof MongoException) {
            return "Não foi possível acessar o banco de dados. Verifique sua conexão com a internet e tente novamente.";
        }
        return "Ocorreu um erro inesperado. Tente novamente.";
    }
}