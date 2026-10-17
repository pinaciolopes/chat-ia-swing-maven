package br.com.projeto.controller;

import br.com.projeto.model.Usuario;
import br.com.projeto.service.AuthenticationService;
import br.com.projeto.util.ErrorHandler;
import br.com.projeto.view.CadastroView;
import javafx.concurrent.Task;

/** Liga a CadastroView à AuthenticationService. Não tem regra de negócio: só encaminha. */
public class CadastroController {

    private final CadastroView view;
    private AuthenticationService servico;

    public CadastroController(CadastroView view) {
        this.view = view;
        view.getBotaoCriar().setOnAction(e -> criarConta());
        view.getLinkLogin().setOnAction(e -> Rotas.irParaLogin());
    }

    private void criarConta() {
        view.limparMensagem();
        String nome = view.getCampoNome().getText();
        String email = view.getCampoEmail().getText();
        String senha = view.getCampoSenha().getText();
        String confirmacao = view.getCampoConfirmacao().getText();

        // BCrypt e banco: roda fora da thread da interface
        Task<Usuario> tarefa = new Task<>() {
            @Override
            protected Usuario call() {
                return servico().cadastrar(nome, email, senha, confirmacao);
            }
        };

        tarefa.setOnSucceeded(e -> {
            view.definirCarregando(false);
            view.limparCampos();
            view.mostrarSucesso("Conta criada com sucesso! Você já pode entrar.");
        });
        tarefa.setOnFailed(e -> {
            view.definirCarregando(false);
            view.limparSenhas();
            view.mostrarErro(ErrorHandler.paraUsuario(tarefa.getException()));
        });

        view.definirCarregando(true);
        Thread thread = new Thread(tarefa, "cadastro");
        thread.setDaemon(true);
        thread.start();
    }

    /** Criado só no primeiro uso e fora da thread da interface (abre a conexão com o MongoDB). */
    private synchronized AuthenticationService servico() {
        if (servico == null) {
            servico = new AuthenticationService();
        }
        return servico;
    }
}