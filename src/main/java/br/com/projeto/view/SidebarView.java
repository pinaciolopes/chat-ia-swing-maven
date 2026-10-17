package br.com.projeto.view;

import br.com.projeto.model.Conversa;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

/** Barra lateral: nova conversa, lista de conversas, usuário, tema e logout. */
public class SidebarView extends VBox {

    private final Button botaoNova = new Button("+  Nova conversa");
    private final ListView<Conversa> lista = new ListView<>();
    private final Label rotuloUsuario = new Label();
    private final Button botaoTema = new Button("Tema escuro");
    private final Button botaoSair = new Button("Sair");

    public SidebarView() {
        getStyleClass().add("sidebar");
        setSpacing(10);
        setPadding(new Insets(14));
        setPrefWidth(260);
        setMinWidth(220);
        setMaxWidth(300);

        botaoNova.getStyleClass().add("botao-primario");
        botaoNova.setMaxWidth(Double.MAX_VALUE);

        Label titulo = new Label("CONVERSAS");
        titulo.getStyleClass().add("rotulo-sidebar");

        lista.getStyleClass().add("lista-conversas");
        lista.setPlaceholder(new Label("Nenhuma conversa ainda"));
        lista.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Conversa conversa, boolean vazio) {
                super.updateItem(conversa, vazio);
                setText(vazio || conversa == null ? null : conversa.getTitle());
            }
        });
        VBox.setVgrow(lista, Priority.ALWAYS);

        rotuloUsuario.getStyleClass().add("usuario-rodape");
        rotuloUsuario.setMaxWidth(Double.MAX_VALUE);

        botaoTema.getStyleClass().add("botao-secundario");
        botaoTema.setMaxWidth(Double.MAX_VALUE);
        botaoSair.getStyleClass().add("botao-secundario");

        HBox acoes = new HBox(8, botaoTema, botaoSair);
        HBox.setHgrow(botaoTema, Priority.ALWAYS);

        getChildren().addAll(botaoNova, titulo, lista, rotuloUsuario, acoes);
    }

    public void definirUsuario(String nome) {
        rotuloUsuario.setText(nome);
    }

    /** Bloqueia a troca de conversa enquanto a IA responde, para a resposta não cair na conversa errada. */
    public void definirNavegacaoHabilitada(boolean habilitada) {
        botaoNova.setDisable(!habilitada);
        lista.setDisable(!habilitada);
    }

    public Button getBotaoNova() { return botaoNova; }
    public ListView<Conversa> getLista() { return lista; }
    public Button getBotaoTema() { return botaoTema; }
    public Button getBotaoSair() { return botaoSair; }
}