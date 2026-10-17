package br.com.projeto.controller;

import br.com.projeto.model.Conversa;
import br.com.projeto.model.Mensagem;
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

        // TEMPORÁRIO: só para ver o layout. O ChatController substitui este bloco.
        view.getSidebar().definirUsuario("Usuario Um");
        Conversa exemplo = new Conversa();
        exemplo.setTitle("Estudando JavaFX");
        view.getSidebar().getLista().getItems().add(exemplo);
        view.definirConversaAtiva(true);
        view.definirTituloConversa("Estudando JavaFX");
        view.adicionarMensagem(Mensagem.PAPEL_USUARIO, "Explique encapsulamento em Java");
        view.adicionarMensagem(Mensagem.PAPEL_ASSISTENTE,
                "Encapsulamento protege o estado interno de um objeto, expondo apenas o que for necessário por meio de métodos.");

        Navegador.mostrar(view, "Chat IA");
    }
}