package br.com.projeto.view;

import br.com.projeto.model.Mensagem;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Tela principal: sidebar à esquerda, conversa à direita. Só monta a interface; as ações ficam no ChatController. */
public class ChatView extends BorderPane {

    private final SidebarView sidebar = new SidebarView();

    private final Label titulo = new Label("Chat IA");
    private final Button botaoRenomear = new Button("Renomear");
    private final Button botaoExcluir = new Button("Excluir");

    private final VBox listaMensagens = new VBox(12);
    private final ScrollPane rolagem = new ScrollPane(listaMensagens);
    private final Label estadoVazio = new Label("Crie uma nova conversa ou selecione uma na lateral.");

    private final Label mensagemErro = new Label();
    private final HBox indicadorEspera = new HBox(8);
    private final TextArea campoMensagem = new TextArea();
    private final Button botaoEnviar = new Button("Enviar");

    private boolean conversaAtiva;

    public ChatView() {
        // ---- cabeçalho da conversa
        titulo.getStyleClass().add("titulo-conversa");
        botaoRenomear.getStyleClass().add("botao-secundario");
        botaoExcluir.getStyleClass().add("botao-perigo");
        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox cabecalho = new HBox(8, titulo, espaco, botaoRenomear, botaoExcluir);
        cabecalho.setAlignment(Pos.CENTER_LEFT);
        cabecalho.getStyleClass().add("cabecalho-chat");

        // ---- área de mensagens
        listaMensagens.getStyleClass().add("lista-mensagens");
        listaMensagens.setPadding(new Insets(20));
        rolagem.getStyleClass().add("area-mensagens");
        rolagem.setFitToWidth(true);
        rolagem.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        // sempre rola até a última mensagem quando o conteúdo cresce
        listaMensagens.heightProperty().addListener((obs, antiga, nova) -> rolagem.setVvalue(1.0));

        estadoVazio.getStyleClass().add("estado-vazio");
        StackPane areaCentral = new StackPane(rolagem, estadoVazio);
        VBox.setVgrow(areaCentral, Priority.ALWAYS);

        // ---- rodapé: erro, indicador de espera e campo de mensagem
        mensagemErro.getStyleClass().add("mensagem-erro");
        mensagemErro.setWrapText(true);
        mensagemErro.setMaxWidth(Double.MAX_VALUE);
        ocultar(mensagemErro);

        ProgressIndicator progresso = new ProgressIndicator();
        progresso.setPrefSize(16, 16);
        Label textoEspera = new Label("A IA está respondendo...");
        textoEspera.getStyleClass().add("texto-espera");
        indicadorEspera.getChildren().addAll(progresso, textoEspera);
        indicadorEspera.setAlignment(Pos.CENTER_LEFT);
        ocultar(indicadorEspera);

        campoMensagem.getStyleClass().add("campo-mensagem");
        campoMensagem.setPromptText("Digite sua pergunta... (Enter envia, Shift + Enter quebra a linha)");
        campoMensagem.setWrapText(true);
        campoMensagem.setPrefRowCount(2);
        botaoEnviar.getStyleClass().add("botao-primario");
        HBox linhaEnvio = new HBox(10, campoMensagem, botaoEnviar);
        linhaEnvio.setAlignment(Pos.BOTTOM_CENTER);
        HBox.setHgrow(campoMensagem, Priority.ALWAYS);

        VBox rodape = new VBox(8, mensagemErro, indicadorEspera, linhaEnvio);
        rodape.getStyleClass().add("rodape-chat");

        VBox painel = new VBox(cabecalho, areaCentral, rodape);
        painel.getStyleClass().add("painel-chat");

        setLeft(sidebar);
        setCenter(painel);

        definirConversaAtiva(false);
    }

    private static void ocultar(javafx.scene.Node no) {
        no.setVisible(false);
        no.setManaged(false);
    }

    private static void exibir(javafx.scene.Node no) {
        no.setVisible(true);
        no.setManaged(true);
    }

    /** Sem conversa selecionada, a área de mensagens e o campo de envio ficam desativados. */
    public void definirConversaAtiva(boolean ativa) {
        this.conversaAtiva = ativa;
        rolagem.setVisible(ativa);
        if (ativa) {
            ocultar(estadoVazio);
        } else {
            exibir(estadoVazio);
            titulo.setText("Chat IA");
            limparMensagens();
        }
        atualizarHabilitacao(false);
    }

    /** Enquanto a IA responde: mostra o indicador e bloqueia envio, renomear e excluir. */
    public void definirCarregando(boolean carregando) {
        if (carregando) {
            exibir(indicadorEspera);
        } else {
            ocultar(indicadorEspera);
        }
        sidebar.definirNavegacaoHabilitada(!carregando);
        atualizarHabilitacao(carregando);
    }

    private void atualizarHabilitacao(boolean carregando) {
        boolean desabilitar = !conversaAtiva || carregando;
        campoMensagem.setDisable(desabilitar);
        botaoEnviar.setDisable(desabilitar);
        botaoRenomear.setDisable(desabilitar);
        botaoExcluir.setDisable(desabilitar);
    }

    public void definirTituloConversa(String texto) {
        titulo.setText(texto);
    }

    public void limparMensagens() {
        listaMensagens.getChildren().clear();
    }

    /** O papel define o lado e a cor do balão: usuário à direita, IA à esquerda. */
    public void adicionarMensagem(String papel, String texto) {
        boolean doUsuario = Mensagem.PAPEL_USUARIO.equals(papel);

        Label balao = new Label(texto);
        balao.setWrapText(true);
        balao.getStyleClass().add(doUsuario ? "balao-usuario" : "balao-ia");
        balao.maxWidthProperty().bind(rolagem.widthProperty().multiply(0.7));

        HBox linha = new HBox(balao);
        linha.setAlignment(doUsuario ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        listaMensagens.getChildren().add(linha);
    }

    public void mostrarErro(String texto) {
        mensagemErro.setText(texto);
        exibir(mensagemErro);
    }

    public void limparErro() {
        mensagemErro.setText("");
        ocultar(mensagemErro);
    }

    public void limparCampoMensagem() {
        campoMensagem.clear();
    }

    public SidebarView getSidebar() { return sidebar; }
    public Button getBotaoRenomear() { return botaoRenomear; }
    public Button getBotaoExcluir() { return botaoExcluir; }
    public TextArea getCampoMensagem() { return campoMensagem; }
    public Button getBotaoEnviar() { return botaoEnviar; }
}