package br.com.projeto.controller;

import br.com.projeto.view.Navegador;
import java.util.prefs.Preferences;
import javafx.scene.control.Dialog;

/** Controla qual folha de estilo (light.css ou dark.css) está ativa e lembra a escolha do usuário. */
public final class TemaController {

    public enum Tema {
        CLARO("/css/light.css"),
        ESCURO("/css/dark.css");

        private final String recurso;

        Tema(String recurso) {
            this.recurso = recurso;
        }

        String url() {
            return TemaController.class.getResource(recurso).toExternalForm();
        }
    }

    private static final String CHAVE = "tema";
    private static final Preferences PREFERENCIAS = Preferences.userNodeForPackage(TemaController.class);

    private static Tema atual = Tema.CLARO;

    private TemaController() {
    }

    /** Aplica o tema salvo (ou o claro, na primeira vez). Chamar depois de Navegador.iniciar. */
    public static void iniciar() {
        Tema salvo;
        try {
            salvo = Tema.valueOf(PREFERENCIAS.get(CHAVE, Tema.CLARO.name()));
        } catch (IllegalArgumentException e) {
            salvo = Tema.CLARO;
        }
        aplicar(salvo);
    }

    public static void alternar() {
        Tema novo = atual == Tema.CLARO ? Tema.ESCURO : Tema.CLARO;
        aplicar(novo);
        PREFERENCIAS.put(CHAVE, novo.name());
    }

    public static Tema getAtual() {
        return atual;
    }

    /** As janelas de diálogo são separadas da cena principal, então recebem o tema à parte. */
    public static void aplicarEm(Dialog<?> dialogo) {
        dialogo.getDialogPane().getStylesheets().setAll(atual.url());
    }

    private static void aplicar(Tema tema) {
        Navegador.getCena().getStylesheets().setAll(tema.url());
        atual = tema;
    }
}