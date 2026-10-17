package br.com.projeto.view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/** Tela de cadastro. Só monta a interface; as ações ficam no CadastroController. */
public class CadastroView extends StackPane {

    private final TextField campoNome = new TextField();
    private final TextField campoEmail = new TextField();
    private final PasswordField campoSenha = new PasswordField();
    private final PasswordField campoConfirmacao = new PasswordField();
    private final Button botaoCriar = new Button("Criar conta");
    private final Hyperlink linkLogin = new Hyperlink("Já tenho conta");
    private final Label mensagem = new Label();
    private final ProgressIndicator indicador = new ProgressIndicator();

    public CadastroView() {
        getStyleClass().add("tela-autenticacao");

        Label titulo = new Label("Criar conta");
        titulo.getStyleClass().add("titulo-app");

        Label subtitulo = new Label("Preencha os dados para começar");
        subtitulo.getStyleClass().add("subtitulo");

        campoNome.setPromptText("Seu nome");
        campoEmail.setPromptText("seu@email.com");
        campoSenha.setPromptText("Mínimo de 8 caracteres");
        campoConfirmacao.setPromptText("Repita a senha");
        for (TextField campo : new TextField[]{campoNome, campoEmail, campoSenha, campoConfirmacao}) {
            campo.getStyleClass().add("campo");
        }

        mensagem.setWrapText(true);
        mensagem.setMaxWidth(Double.MAX_VALUE);
        esconderMensagem();

        botaoCriar.getStyleClass().add("botao-primario");
        botaoCriar.setMaxWidth(Double.MAX_VALUE);
        botaoCriar.setDefaultButton(true); // Enter aciona o botão

        indicador.setMaxSize(24, 24);
        indicador.setVisible(false);
        indicador.setManaged(false);

        VBox cartao = new VBox(8,
                titulo, subtitulo,
                rotulo("Nome"), campoNome,
                rotulo("E-mail"), campoEmail,
                rotulo("Senha"), campoSenha,
                rotulo("Confirmar senha"), campoConfirmacao,
                mensagem, botaoCriar, indicador, linkLogin);
        cartao.getStyleClass().add("cartao");
        cartao.setAlignment(Pos.CENTER_LEFT);
        cartao.setPadding(new Insets(28, 32, 24, 32));
        cartao.setMaxWidth(400);
        cartao.setMaxHeight(Region.USE_PREF_SIZE);

        getChildren().add(cartao);
    }

    private Label rotulo(String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("rotulo-campo");
        return rotulo;
    }

    public void mostrarErro(String texto) {
        exibir(texto, "mensagem-erro");
    }

    public void mostrarSucesso(String texto) {
        exibir(texto, "mensagem-sucesso");
    }

    public void limparMensagem() {
        esconderMensagem();
    }

    private void exibir(String texto, String estilo) {
        mensagem.getStyleClass().removeAll("mensagem-erro", "mensagem-sucesso");
        mensagem.getStyleClass().add(estilo);
        mensagem.setText(texto);
        mensagem.setVisible(true);
        mensagem.setManaged(true);
    }

    private void esconderMensagem() {
        mensagem.setText("");
        mensagem.setVisible(false);
        mensagem.setManaged(false);
    }

    public void definirCarregando(boolean carregando) {
        campoNome.setDisable(carregando);
        campoEmail.setDisable(carregando);
        campoSenha.setDisable(carregando);
        campoConfirmacao.setDisable(carregando);
        botaoCriar.setDisable(carregando);
        indicador.setVisible(carregando);
        indicador.setManaged(carregando);
    }

    public void limparSenhas() {
        campoSenha.clear();
        campoConfirmacao.clear();
    }

    public void limparCampos() {
        campoNome.clear();
        campoEmail.clear();
        limparSenhas();
    }

    public TextField getCampoNome() { return campoNome; }
    public TextField getCampoEmail() { return campoEmail; }
    public PasswordField getCampoSenha() { return campoSenha; }
    public PasswordField getCampoConfirmacao() { return campoConfirmacao; }
    public Button getBotaoCriar() { return botaoCriar; }
    public Hyperlink getLinkLogin() { return linkLogin; }
}