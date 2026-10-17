package br.com.projeto.view;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/** Controla a janela única da aplicação e troca a tela exibida. Use apenas na thread do JavaFX. */
public final class Navegador {

    private static Stage palco;
    private static Scene cena;

    private Navegador() {
    }

    public static void iniciar(Stage stage, double largura, double altura) {
        palco = stage;
        cena = new Scene(new StackPane(), largura, altura);
        palco.setScene(cena);
        palco.setMinWidth(800);
        palco.setMinHeight(620);
    }

    public static void mostrar(Parent tela, String titulo) {
        exigirIniciado();
        cena.setRoot(tela);
        palco.setTitle(titulo);
        if (!palco.isShowing()) {
            palco.show();
        }
    }

    /** Usada mais adiante para aplicar o tema claro ou escuro. */
    public static Scene getCena() {
        exigirIniciado();
        return cena;
    }

    private static void exigirIniciado() {
        if (palco == null || cena == null) {
            throw new IllegalStateException("O Navegador não foi iniciado. Chame Navegador.iniciar(...) na Main.");
        }
    }
}