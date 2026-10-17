package br.com.projeto.util;

import br.com.projeto.config.MongoConfig;
import br.com.projeto.service.AuthenticationService;

public class CriarUsuarioTeste {

    public static void main(String[] args) {
        try {
            new AuthenticationService().cadastrar("Usuario Um", "usuario1@teste.com", "senha12345", "senha12345");
            System.out.println("Usuário criado.");
        } catch (AppException e) {
            System.out.println("Aviso: " + e.getMessage());
        } finally {
            MongoConfig.close();
        }
    }
}