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
        view.getLinkCadastro().setOnAction(e -> Rotas.irParaCadastro());
        view.getLinkEsqueciSenha().setOnAction(e -> Rotas.irParaRecuperarSenha());
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
            Rotas.irParaChat();
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
}