package br.com.projeto;

import br.com.projeto.config.MongoConfig;
import br.com.projeto.controller.Rotas;
import br.com.projeto.controller.TemaController;
import br.com.projeto.view.Navegador;
import br.com.projeto.view.SplashView;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Navegador.iniciar(stage, 1000, 650);
        TemaController.iniciar();

        SplashView splash = new SplashView();
        Navegador.mostrar(splash, "Chat IA");
        splash.iniciar(Rotas::irParaLogin);
        stage.getIcons().add(new Image(Main.class.getResourceAsStream("/images/icone.png")));
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