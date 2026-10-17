package br.com.projeto.controller;

import br.com.projeto.service.RecuperacaoSenhaService;
import br.com.projeto.util.ErrorHandler;
import br.com.projeto.view.RecuperarSenhaView;
import java.util.Optional;
import javafx.concurrent.Task;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;

/** Liga a RecuperarSenhaView à RecuperacaoSenhaService. Não tem regra de negócio: só encaminha. */
public class RecuperarSenhaController {

    private final RecuperarSenhaView view;
    private RecuperacaoSenhaService servico;

    public RecuperarSenhaController(RecuperarSenhaView view) {
        this.view = view;
        view.getBotaoGerar().setOnAction(e -> gerarCodigo());
        view.getCampoEmail().setOnAction(e -> gerarCodigo());              // Enter na etapa 1
        view.getBotaoRedefinir().setOnAction(e -> redefinirSenha());
        view.getCampoConfirmacao().setOnAction(e -> redefinirSenha());     // Enter na etapa 2
        view.getBotaoCopiar().setOnAction(e -> copiarCodigo());
        view.getLinkVoltar().setOnAction(e -> Rotas.irParaLogin());
    }

    private void gerarCodigo() {
        view.limparMensagem();
        String email = view.getCampoEmail().getText();

        Task<Optional<String>> tarefa = new Task<>() {
            @Override
            protected Optional<String> call() {
                return servico().solicitar(email);
            }
        };

        tarefa.setOnSucceeded(e -> {
            view.definirCarregando(false);
            // a mensagem é a mesma exista o e-mail ou não, para não revelar quem está cadastrado
            view.mostrarEtapaRedefinicao(tarefa.getValue().orElse(null));
            view.mostrarSucesso("Se o e-mail estiver cadastrado, um código de recuperação foi gerado. Ele vale por 15 minutos.");
        });
        tarefa.setOnFailed(e -> {
            view.definirCarregando(false);
            view.mostrarErro(ErrorHandler.paraUsuario(tarefa.getException()));
        });

        executar(tarefa, "recuperacao-solicitar");
    }

    private void redefinirSenha() {
        view.limparMensagem();
        String codigo = view.getCampoCodigo().getText();
        String novaSenha = view.getCampoNovaSenha().getText();
        String confirmacao = view.getCampoConfirmacao().getText();

        Task<Void> tarefa = new Task<>() {
            @Override
            protected Void call() {
                servico().redefinirSenha(codigo, novaSenha, confirmacao);
                return null;
            }
        };

        tarefa.setOnSucceeded(e -> {
            view.definirCarregando(false);
            view.concluirRedefinicao();
            view.mostrarSucesso("Senha redefinida com sucesso! Volte ao login para entrar.");
        });
        tarefa.setOnFailed(e -> {
            view.definirCarregando(false);
            view.limparSenhas();
            view.mostrarErro(ErrorHandler.paraUsuario(tarefa.getException()));
        });

        executar(tarefa, "recuperacao-redefinir");
    }

    private void copiarCodigo() {
        ClipboardContent conteudo = new ClipboardContent();
        conteudo.putString(view.getCampoCodigoGerado().getText());
        Clipboard.getSystemClipboard().setContent(conteudo);
        view.mostrarSucesso("Código copiado para a área de transferência.");
    }

    private void executar(Task<?> tarefa, String nome) {
        view.definirCarregando(true);
        Thread thread = new Thread(tarefa, nome);
        thread.setDaemon(true);
        thread.start();
    }

    /** Criado só no primeiro uso e fora da thread da interface (abre a conexão com o MongoDB). */
    private synchronized RecuperacaoSenhaService servico() {
        if (servico == null) {
            servico = new RecuperacaoSenhaService();
        }
        return servico;
    }
}