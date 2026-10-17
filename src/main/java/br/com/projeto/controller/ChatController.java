package br.com.projeto.controller;

import br.com.projeto.model.Conversa;
import br.com.projeto.model.Mensagem;
import br.com.projeto.service.ChatService;
import br.com.projeto.service.ConversaService;
import br.com.projeto.service.SessionManagerService;
import br.com.projeto.util.ErrorHandler;
import br.com.projeto.view.ChatView;
import br.com.projeto.view.SidebarView;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import org.bson.types.ObjectId;

/** Liga a ChatView aos services. Não tem regra de negócio: só encaminha e mostra o resultado. */
public class ChatController {

    private final ChatView view;
    private final SidebarView sidebar;

    private ConversaService conversaService;
    private ChatService chatService;

    private Conversa conversaAtual;
    private boolean atualizandoLista; // evita reagir às mudanças de seleção feitas pelo próprio código

    public ChatController(ChatView view) {
        this.view = view;
        this.sidebar = view.getSidebar();

        sidebar.definirUsuario(SessionManagerService.getInstance().exigirUsuario().getName());

        sidebar.getBotaoNova().setOnAction(e -> novaConversa());
        sidebar.getBotaoSair().setOnAction(e -> sair());
        sidebar.getLista().getSelectionModel().selectedItemProperty().addListener((obs, antiga, nova) -> {
            if (atualizandoLista) {
                return;
            }
            if (nova != null) {
                abrirConversa(nova);
            } else {
                fecharConversa();
            }
        });

        view.getBotaoRenomear().setOnAction(e -> renomear());
        view.getBotaoExcluir().setOnAction(e -> excluir());
        view.getBotaoEnviar().setOnAction(e -> enviar());
        view.getCampoMensagem().addEventFilter(KeyEvent.KEY_PRESSED, this::tratarTeclas);

        carregarConversas(null);
    }

    // ---------- conversas ----------

    /** Recarrega a lista. Seleciona a conversa do id informado; com null, seleciona a mais recente. */
    private void carregarConversas(ObjectId idParaSelecionar) {
        executar("listar-conversas", () -> conversaService().listarMinhas(), conversas -> {
            atualizandoLista = true;
            sidebar.getLista().getItems().setAll(conversas);
            atualizandoLista = false;

            Conversa escolhida = null;
            if (idParaSelecionar != null) {
                escolhida = conversas.stream()
                        .filter(c -> c.getId().equals(idParaSelecionar))
                        .findFirst().orElse(null);
            } else if (!conversas.isEmpty()) {
                escolhida = conversas.get(0);
            }

            if (escolhida == null) {
                fecharConversa();
                return;
            }

            atualizandoLista = true;
            sidebar.getLista().getSelectionModel().select(escolhida);
            atualizandoLista = false;

            boolean mesmaConversa = conversaAtual != null && conversaAtual.getId().equals(escolhida.getId());
            if (mesmaConversa) {
                conversaAtual = escolhida; // só atualiza título e data, sem recarregar as mensagens
                view.definirTituloConversa(escolhida.getTitle());
            } else {
                abrirConversa(escolhida);
            }
        }, this::mostrarFalha);
    }

    private void abrirConversa(Conversa conversa) {
        conversaAtual = conversa;
        view.limparErro();
        view.limparMensagens();
        view.definirConversaAtiva(true);
        view.definirTituloConversa(conversa.getTitle());

        executar("carregar-mensagens", () -> conversaService().carregarMensagens(conversa.getId()), mensagens -> {
            if (conversaAtual != null && conversaAtual.getId().equals(conversa.getId())) {
                view.limparMensagens();
                mensagens.forEach(m -> view.adicionarMensagem(m.getRole(), m.getContent()));
            }
        }, this::mostrarFalha);
    }

    private void fecharConversa() {
        conversaAtual = null;
        view.limparErro();
        view.definirConversaAtiva(false);
    }

    private void novaConversa() {
        view.limparErro();
        executar("nova-conversa", () -> conversaService().criar(),
                conversa -> carregarConversas(conversa.getId()), this::mostrarFalha);
    }

    private void renomear() {
        if (conversaAtual == null) {
            return;
        }
        Conversa alvo = conversaAtual;

        TextInputDialog dialogo = new TextInputDialog(alvo.getTitle());
        dialogo.initOwner(view.getScene().getWindow());
        dialogo.setTitle("Renomear conversa");
        dialogo.setHeaderText("Digite o novo título da conversa");
        dialogo.setContentText("Título:");

        dialogo.showAndWait().ifPresent(novoTitulo -> {
            view.limparErro();
            executar("renomear", () -> {
                conversaService().renomear(alvo.getId(), novoTitulo);
                return alvo.getId();
            }, this::carregarConversas, this::mostrarFalha);
        });
    }

    private void excluir() {
        if (conversaAtual == null) {
            return;
        }
        Conversa alvo = conversaAtual;

        ButtonType confirmar = new ButtonType("Excluir", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        Alert alerta = new Alert(Alert.AlertType.CONFIRMATION);
        alerta.initOwner(view.getScene().getWindow());
        alerta.setTitle("Excluir conversa");
        alerta.setHeaderText("Excluir a conversa \"" + alvo.getTitle() + "\"?");
        alerta.setContentText("Todas as mensagens serão apagadas. Esta ação não pode ser desfeita.");
        alerta.getButtonTypes().setAll(confirmar, cancelar);

        alerta.showAndWait().filter(botao -> botao == confirmar).ifPresent(botao -> {
            view.limparErro();
            executar("excluir", () -> {
                conversaService().excluir(alvo.getId());
                return alvo.getId();
            }, id -> {
                conversaAtual = null;
                carregarConversas(null);
            }, this::mostrarFalha);
        });
    }

    // ---------- mensagens ----------

    private void enviar() {
        if (conversaAtual == null) {
            return;
        }
        String pergunta = view.getCampoMensagem().getText() == null ? "" : view.getCampoMensagem().getText().trim();
        if (pergunta.isEmpty()) {
            view.mostrarErro("Digite uma pergunta.");
            return;
        }
        if (pergunta.length() > ChatService.TAMANHO_MAXIMO_MENSAGEM) {
            view.mostrarErro("A mensagem é muito longa (máximo de " + ChatService.TAMANHO_MAXIMO_MENSAGEM + " caracteres).");
            return;
        }

        Conversa conversa = conversaAtual;
        view.limparErro();
        view.adicionarMensagem(Mensagem.PAPEL_USUARIO, pergunta);
        view.limparCampoMensagem();
        view.definirCarregando(true);

        executar("enviar-mensagem", () -> chatService().enviar(conversa.getId(), pergunta), resposta -> {
            view.definirCarregando(false);
            view.adicionarMensagem(Mensagem.PAPEL_ASSISTENTE, resposta.getContent());
            carregarConversas(conversa.getId()); // a conversa sobe na lista (data de atualização)
        }, erro -> {
            view.definirCarregando(false);
            mostrarFalha(erro);
        });
    }

    /** Enter envia; Shift + Enter quebra a linha. */
    private void tratarTeclas(KeyEvent evento) {
        if (evento.getCode() != KeyCode.ENTER) {
            return;
        }
        evento.consume();
        if (evento.isShiftDown()) {
            TextArea campo = view.getCampoMensagem();
            campo.insertText(campo.getCaretPosition(), "\n");
        } else {
            enviar();
        }
    }

    // ---------- sessão ----------

    private void sair() {
        SessionManagerService.getInstance().encerrar();
        Rotas.irParaLogin();
    }

    // ---------- apoio ----------

    private void mostrarFalha(Throwable erro) {
        view.mostrarErro(ErrorHandler.paraUsuario(erro));
    }

    /** Roda o trabalho em outra thread; os dois retornos voltam para a thread da interface. */
    private <T> void executar(String nome, Callable<T> trabalho, Consumer<T> aoConcluir, Consumer<Throwable> aoFalhar) {
        Task<T> tarefa = new Task<>() {
            @Override
            protected T call() throws Exception {
                return trabalho.call();
            }
        };
        tarefa.setOnSucceeded(e -> aoConcluir.accept(tarefa.getValue()));
        tarefa.setOnFailed(e -> aoFalhar.accept(tarefa.getException()));

        Thread thread = new Thread(tarefa, nome);
        thread.setDaemon(true);
        thread.start();
    }

    /** Criados só no primeiro uso e fora da thread da interface: abrem a conexão com o MongoDB. */
    private synchronized ConversaService conversaService() {
        if (conversaService == null) {
            conversaService = new ConversaService();
        }
        return conversaService;
    }

    private synchronized ChatService chatService() {
        if (chatService == null) {
            chatService = new ChatService();
        }
        return chatService;
    }
}