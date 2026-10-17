package br.com.projeto.controller;

import br.com.projeto.view.*;

/** Um método por tela: monta a View, liga o Controller e exibe. */
public final class Rotas {

    private Rotas() {
    }

    public static void irParaLogin() {
        LoginView view = new LoginView();
        new LoginController(view);
        Navegador.mostrar(view, "Chat IA - Login");
    }

    public static void irParaCadastro() {
        CadastroView view = new CadastroView();
        new CadastroController(view);
        Navegador.mostrar(view, "Chat IA - Criar conta");
    }

    public static void irParaRecuperarSenha() {
        RecuperarSenhaView view = new RecuperarSenhaView();
        new RecuperarSenhaController(view);
        Navegador.mostrar(view, "Chat IA - Recuperar senha");
    }

    public static void irParaChat() {
        ChatView view = new ChatView();
        new ChatController(view);
        Navegador.mostrar(view, "Chat IA");
    }
}