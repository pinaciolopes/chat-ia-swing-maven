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

/** Abertura da aplicação: exibe a animação GSAP (HTML) dentro de um WebView. */
public class SplashView extends StackPane {

    /** Título que o JavaScript define quando a animação termina. */
    private static final String TITULO_FIM = "splash-concluido";

    /** Se algo falhar (arquivo ausente, JavaScript com erro), o login abre depois deste tempo. */
    private static final double TEMPO_MAXIMO_SEGUNDOS = 8;

    private final WebView webView = new WebView();
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

        // o JavaScript muda o título da página para avisar que a animação acabou
        engine.titleProperty().addListener((obs, antigo, novo) -> {
            if (TITULO_FIM.equals(novo)) {
                Platform.runLater(this::concluir); // sai do callback do WebView antes de trocar de tela
            }
        });
        engine.getLoadWorker().stateProperty().addListener((obs, antigo, novo) -> {
            if (novo == Worker.State.FAILED) {
                Platform.runLater(this::concluir);
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
}