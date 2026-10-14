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

/** Tela de login. Só monta a interface; as ações ficam no LoginController. */
public class LoginView extends StackPane {

    private final TextField campoEmail = new TextField();
    private final PasswordField campoSenha = new PasswordField();
    private final Button botaoEntrar = new Button("Entrar");
    private final Hyperlink linkCadastro = new Hyperlink("Criar conta");
    private final Hyperlink linkEsqueciSenha = new Hyperlink("Esqueci minha senha");
    private final Label mensagemErro = new Label();
    private final ProgressIndicator indicador = new ProgressIndicator();

    public LoginView() {
        getStyleClass().add("tela-autenticacao");

        Label titulo = new Label("Chat IA");
        titulo.getStyleClass().add("titulo-app");

        Label subtitulo = new Label("Entre na sua conta");
        subtitulo.getStyleClass().add("subtitulo");

        campoEmail.setPromptText("seu@email.com");
        campoEmail.getStyleClass().add("campo");

        campoSenha.setPromptText("Sua senha");
        campoSenha.getStyleClass().add("campo");

        mensagemErro.getStyleClass().add("mensagem-erro");
        mensagemErro.setWrapText(true);
        mensagemErro.setMaxWidth(Double.MAX_VALUE);
        mensagemErro.setVisible(false);
        mensagemErro.setManaged(false);

        botaoEntrar.getStyleClass().add("botao-primario");
        botaoEntrar.setMaxWidth(Double.MAX_VALUE);
        botaoEntrar.setDefaultButton(true); // Enter aciona o botão

        indicador.setMaxSize(24, 24);
        indicador.setVisible(false);
        indicador.setManaged(false);

        Region espaco = new Region();
        HBox.setHgrow(espaco, Priority.ALWAYS);
        HBox links = new HBox(linkCadastro, espaco, linkEsqueciSenha);
        links.setAlignment(Pos.CENTER_LEFT);

        VBox cartao = new VBox(10,
                titulo, subtitulo,
                rotulo("E-mail"), campoEmail,
                rotulo("Senha"), campoSenha,
                mensagemErro, botaoEntrar, indicador, links);
        cartao.getStyleClass().add("cartao");
        cartao.setAlignment(Pos.CENTER_LEFT);
        cartao.setPadding(new Insets(32));
        cartao.setMaxWidth(400);
        cartao.setMaxHeight(Region.USE_PREF_SIZE); // o cartão não estica na vertical

        VBox.setMargin(indicador, new Insets(0));
        getChildren().add(cartao);
    }

    private Label rotulo(String texto) {
        Label rotulo = new Label(texto);
        rotulo.getStyleClass().add("rotulo-campo");
        return rotulo;
    }

    public void mostrarErro(String mensagem) {
        mensagemErro.setText(mensagem);
        mensagemErro.setVisible(true);
        mensagemErro.setManaged(true);
    }

    public void limparErro() {
        mensagemErro.setText("");
        mensagemErro.setVisible(false);
        mensagemErro.setManaged(false);
    }

    /** Durante uma operação demorada: bloqueia os campos e mostra o indicador de progresso. */
    public void definirCarregando(boolean carregando) {
        campoEmail.setDisable(carregando);
        campoSenha.setDisable(carregando);
        botaoEntrar.setDisable(carregando);
        indicador.setVisible(carregando);
        indicador.setManaged(carregando);
    }

    public TextField getCampoEmail() { return campoEmail; }
    public PasswordField getCampoSenha() { return campoSenha; }
    public Button getBotaoEntrar() { return botaoEntrar; }
    public Hyperlink getLinkCadastro() { return linkCadastro; }
    public Hyperlink getLinkEsqueciSenha() { return linkEsqueciSenha; }
}