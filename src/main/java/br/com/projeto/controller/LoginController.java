package br.com.projeto.controller;

import br.com.projeto.model.Usuario;
import br.com.projeto.service.AuthenticationService;
import br.com.projeto.util.ErrorHandler;
import br.com.projeto.util.ErrorHandler;
import br.com.projeto.view.LoginView;
import br.com.projeto.view.Navegador;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

/** Liga a LoginView à AuthenticationService. Não tem regra de negócio: só encaminha. */
public class LoginController {

    private final LoginView view;
    private AuthenticationService servico;

    public LoginController(LoginView view) {
        this.view = view;
        view.getBotaoEntrar().setOnAction(e -> entrar());
        // os links "Criar conta" e "Esqueci minha senha" serão ligados nas próximas telas
    }

    private void entrar() {
        view.limparErro();
        String email = view.getCampoEmail().getText();
        String senha = view.getCampoSenha().getText();

        // o login usa BCrypt e o banco: roda fora da thread da interface para a janela não congelar
        Task<Usuario> tarefa = new Task<>() {
            @Override
            protected Usuario call() {
                return servico().login(email, senha);
            }
        };

        tarefa.setOnSucceeded(e -> {
            view.definirCarregando(false);
            view.getCampoSenha().clear();
            abrirTelaPrincipal(tarefa.getValue());
        });
        tarefa.setOnFailed(e -> {
            view.definirCarregando(false);
            view.getCampoSenha().clear();
            view.mostrarErro(ErrorHandler.paraUsuario(tarefa.getException()));
        });

        view.definirCarregando(true);
        Thread thread = new Thread(tarefa, "login");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * Criado só no primeiro uso e fora da thread da interface: o construtor da service abre a conexão
     * com o MongoDB, que pode demorar ou falhar. Se falhar, a próxima tentativa tenta de novo.
     */
    private synchronized AuthenticationService servico() {
        if (servico == null) {
            servico = new AuthenticationService();
        }
        return servico;
    }

    /** Provisório: será substituído pela ChatView. */
    private void abrirTelaPrincipal(Usuario usuario) {
        Label boasVindas = new Label("Login realizado! Bem-vindo, " + usuario.getName());
        Navegador.mostrar(new StackPane(boasVindas), "Chat IA");
    }
}