package br.com.projeto.view;

import java.net.URL;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Worker;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import netscape.javascript.JSObject;

/** Abertura da aplicação: exibe a animação GSAP (HTML) dentro de um WebView. */
public class SplashView extends StackPane {

    /** Se algo falhar (arquivo ausente, JavaScript com erro), o login abre depois deste tempo. */
    private static final double TEMPO_MAXIMO_SEGUNDOS = 8;

    private final WebView webView = new WebView();
    private final PonteSplash ponte = new PonteSplash(this::concluir);
    private final PauseTransition limiteDeSeguranca = new PauseTransition(Duration.seconds(TEMPO_MAXIMO_SEGUNDOS));

    private Runnable aoTerminar;
    private boolean concluida;

    public SplashView() {
        webView.setContextMenuEnabled(false);
        webView.setPageFill(Color.web("#0f172a")); // evita um flash branco antes do HTML carregar
        getChildren().add(webView);
    }

    /** Carrega a animação; quando ela terminar, executa o que foi passado. */
    public void iniciar(Runnable aoTerminar) {
        this.aoTerminar = aoTerminar;

        URL pagina = getClass().getResource("/splash/html/splash.html");
        if (pagina == null) {
            concluir();
            return;
        }

        WebEngine engine = webView.getEngine();
        engine.getLoadWorker().stateProperty().addListener((obs, antigo, novo) -> {
            if (novo == Worker.State.SUCCEEDED) {
                // expõe a ponte ao JavaScript como window.app
                JSObject janela = (JSObject) engine.executeScript("window");
                janela.setMember("app", ponte);
            } else if (novo == Worker.State.FAILED) {
                concluir();
            }
        });

        limiteDeSeguranca.setOnFinished(e -> concluir());
        limiteDeSeguranca.play();
        engine.load(pagina.toExternalForm());
    }

    private void concluir() {
        if (concluida) {
            return;
        }
        concluida = true;
        limiteDeSeguranca.stop();
        if (aoTerminar != null) {
            aoTerminar.run();
        }
    }

    /** Classe chamada pelo JavaScript. Precisa ser pública, assim como o método. */
    public static class PonteSplash {

        private final Runnable aoFinalizar;

        public PonteSplash(Runnable aoFinalizar) {
            this.aoFinalizar = aoFinalizar;
        }

        public void finalizar() {
            // sai do callback do JavaScript antes de trocar de tela
            Platform.runLater(aoFinalizar);
        }
    }

}