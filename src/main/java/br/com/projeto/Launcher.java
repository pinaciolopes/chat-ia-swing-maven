package br.com.projeto;

/**
 * Ponto de entrada real da aplicação. Existe só para chamar a Main.
 * Uma classe que estende Application não inicia bem quando o JavaFX está no classpath
 * (IntelliJ e jar do jpackage); uma classe comum como esta resolve.
 */
public class Launcher {

    public static void main(String[] args) {
        Main.main(args);
    }
}