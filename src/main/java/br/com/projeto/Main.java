package br.com.projeto;

import br.com.projeto.config.MongoConfig;
import br.com.projeto.controller.Rotas;
import br.com.projeto.view.Navegador;
import br.com.projeto.view.SplashView;
import javafx.application.Application;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        Navegador.iniciar(stage, 1000, 650);

        // provisório: o tema será controlado por uma classe própria mais adiante
        Navegador.getCena().getStylesheets()
                .add(Main.class.getResource("/css/light.css").toExternalForm());

        SplashView splash = new SplashView();
        Navegador.mostrar(splash, "Chat IA");
        splash.iniciar(Rotas::irParaLogin);
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