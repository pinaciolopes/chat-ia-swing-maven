package br.com.projeto.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Recuperação de senha em duas etapas. Só monta a interface; as ações ficam no controller. */
public class RecuperarSenhaView extends StackPane {

    // etapa 1
    private final TextField campoEmail = new TextField();
    private final Button botaoGerar = new Button("Gerar código");
    private final VBox etapaEmail;

    // etapa 2
    private final TextField campoCodigoGerado = new TextField();
    private final Button botaoCopiar = new Button("Copiar");
    private final VBox caixaCodigo = new VBox(6);
    private final TextField campoCodigo = new TextField();
    private final PasswordField campoNovaSenha = new PasswordField();
    private final PasswordField campoConfirmacao = new PasswordField();
    private final Button botaoRedefinir = new Button("Redefinir senha");
    private final VBox etapaRedefinicao;

    private final Label subtitulo = new Label("Informe seu e-mail para gerar um código de recuperação");
    private final Label mensagem = new Label();
    private final ProgressIndicator indicador = new ProgressIndicator();
    private final Hyperlink linkVoltar = new Hyperlink("Voltar ao login");

    public RecuperarSenhaView() {
        getStyleClass().add("tela-autenticacao");

        Label titulo = new Label("Recuperar senha");
        titulo.getStyleClass().add("titulo-app");
        subtitulo.getStyleClass().add("subtitulo");
        subtitulo.setWrapText(true);

        campoEmail.setPromptText("seu@email.com");
        campoEmail.getStyleClass().add("campo");
        botaoGerar.getStyleClass().add("botao-primario");
        botaoGerar.setMaxWidth(Double.MAX_VALUE);
        etapaEmail = new VBox(8, rotulo("E-mail"), campoEmail, botaoGerar);

        // caixa de demonstração: mostra o código porque a aplicação não envia e-mail
        Label avisoDemo = new Label("Modo demonstração: a aplicação não envia e-mail, então o código aparece aqui.");
        avisoDemo.getStyleClass().add("texto-demo");
        avisoDemo.setWrapText(true);
        campoCodigoGerado.setEditable(false);
        campoCodigoGerado.getStyleClass().add("campo");
        botaoCopiar.getStyleClass().add("botao-secundario");
        HBox linhaCodigo = new HBox(8, campoCodigoGerado, botaoCopiar);
        linhaCodigo.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(campoCodigoGerado, Priority.ALWAYS);
        caixaCodigo.getChildren().addAll(avisoDemo, linhaCodigo);
        caixaCodigo.getStyleClass().add("caixa-demo");
        ocultar(caixaCodigo);

        campoCodigo.setPromptText("Cole o código recebido");
        campoCodigo.getStyleClass().add("campo");
        campoNovaSenha.setPromptText("Mínimo de 8 caracteres");
        campoNovaSenha.getStyleClass().add("campo");
        campoConfirmacao.setPromptText("Repita a nova senha");
        campoConfirmacao.getStyleClass().add("campo");
        botaoRedefinir.getStyleClass().add("botao-primario");
        botaoRedefinir.setMaxWidth(Double.MAX_VALUE);
        etapaRedefinicao = new VBox(8,
                caixaCodigo,
                rotulo("Código de recuperação"), campoCodigo,
                rotulo("Nova senha"), campoNovaSenha,
                rotulo("Confirmar nova senha"), campoConfirmacao,
                botaoRedefinir);
        ocultar(etapaRedefinicao);

        mensagem.setWrapText(true);
        mensagem.setMaxWidth(Double.MAX_VALUE);
        ocultar(mensagem);

        indicador.setMaxSize(24, 24);
        ocultar(indicador);

        VBox cartao = new VBox(8, titulo, subtitulo, etapaEmail, etapaRedefinicao, mensagem, indicador, linkVoltar);
        cartao.getStyleClass().add("cartao");
        cartao.setAlignment(Pos.CENTER_LEFT);
        cartao.setPadding(new Insets(28, 32, 24, 32));
        cartao.setMaxWidth(420);
        cartao.setMaxHeight(Region.USE_PREF_SIZE);

        getChildren().add(cartao);
    }

    private Label rotulo(String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("rotulo-campo");
        return rotulo;
    }

    private static void ocultar(javafx.scene.Node no) {
        no.setVisible(false);
        no.setManaged(false);
    }

    private static void exibir(javafx.scene.Node no) {
        no.setVisible(true);
        no.setManaged(true);
    }

    /** Troca para a etapa 2. Se o código for nulo (e-mail não cadastrado), a caixa de demonstração não aparece. */
    public void mostrarEtapaRedefinicao(String codigoGerado) {
        ocultar(etapaEmail);
        exibir(etapaRedefinicao);
        subtitulo.setText("Digite o código e escolha a nova senha");
        if (codigoGerado != null) {
            campoCodigoGerado.setText(codigoGerado);
            exibir(caixaCodigo);
        } else {
            ocultar(caixaCodigo);
        }
    }

    public void mostrarErro(String texto) {
        mostrar(texto, "mensagem-erro");
    }

    public void mostrarSucesso(String texto) {
        mostrar(texto, "mensagem-sucesso");
    }

    public void limparMensagem() {
        mensagem.setText("");
        ocultar(mensagem);
    }

    private void mostrar(String texto, String estilo) {
        mensagem.getStyleClass().removeAll("mensagem-erro", "mensagem-sucesso");
        mensagem.getStyleClass().add(estilo);
        mensagem.setText(texto);
        exibir(mensagem);
    }

    public void definirCarregando(boolean carregando) {
        campoEmail.setDisable(carregando);
        botaoGerar.setDisable(carregando);
        campoCodigo.setDisable(carregando);
        campoNovaSenha.setDisable(carregando);
        campoConfirmacao.setDisable(carregando);
        botaoRedefinir.setDisable(carregando);
        if (carregando) {
            exibir(indicador);
        } else {
            ocultar(indicador);
        }
    }

    public void limparSenhas() {
        campoNovaSenha.clear();
        campoConfirmacao.clear();
    }

    /** Depois de redefinir: apaga tudo e desliga a etapa para o código não ser reutilizado na tela. */
    public void concluirRedefinicao() {
        campoCodigo.clear();
        limparSenhas();
        campoCodigoGerado.clear();
        ocultar(caixaCodigo);
        etapaRedefinicao.setDisable(true);
    }

    public TextField getCampoEmail() { return campoEmail; }
    public Button getBotaoGerar() { return botaoGerar; }
    public TextField getCampoCodigoGerado() { return campoCodigoGerado; }
    public Button getBotaoCopiar() { return botaoCopiar; }
    public TextField getCampoCodigo() { return campoCodigo; }
    public PasswordField getCampoNovaSenha() { return campoNovaSenha; }
    public PasswordField getCampoConfirmacao() { return campoConfirmacao; }
    public Button getBotaoRedefinir() { return botaoRedefinir; }
    public Hyperlink getLinkVoltar() { return linkVoltar; }
}