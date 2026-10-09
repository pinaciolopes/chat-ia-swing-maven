package br.com.projeto;

import br.com.projeto.config.MongoConfig;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        // tela provisória, só para confirmar que o JavaFX funciona
        StackPane raiz = new StackPane(new Label("JavaFX funcionando!"));

        stage.setTitle("Chat IA");
        stage.setScene(new Scene(raiz, 900, 600));
        stage.show();
    }

    /** Chamado quando a janela principal é fechada: libera a conexão com o MongoDB. */
    @Override
    public void stop() {
        MongoConfig.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}