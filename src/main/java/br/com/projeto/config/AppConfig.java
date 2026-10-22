package br.com.projeto.config;

import br.com.projeto.util.AppException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Lê configurações externas: primeiro variáveis de ambiente, depois o arquivo .env. */
public final class AppConfig {

    private static final String ARQUIVO = ".env";
    private static Map<String, String> valoresDoArquivo; // carregado uma única vez

    private AppConfig() {
    }

    public static String require(String name) {
        String valor = System.getenv(name);
        if (valor == null || valor.isBlank()) {
            valor = doArquivo(name);
        }
        if (valor == null || valor.isBlank()) {
            throw new AppException("Configuração ausente: defina a variável de ambiente " + name
                    + " ou o arquivo " + ARQUIVO + " na pasta do programa.");
        }
        return valor.trim();
    }

    private static synchronized String doArquivo(String name) {
        if (valoresDoArquivo == null) {
            valoresDoArquivo = carregarArquivo();
        }
        return valoresDoArquivo.get(name);
    }

    private static Map<String, String> carregarArquivo() {
        Map<String, String> valores = new HashMap<>();
        for (Path caminho : locais()) {
            if (!Files.isRegularFile(caminho)) {
                continue;
            }
            try {
                for (String linha : Files.readAllLines(caminho, StandardCharsets.UTF_8)) {
                    interpretar(linha, valores);
                }
                break; // usa o primeiro arquivo encontrado
            } catch (IOException e) {
                // não conseguiu ler este arquivo: tenta o próximo local
            }
        }
        return valores;
    }

    /** Aceita linhas CHAVE=VALOR, comentários com #, "export CHAVE=..." e valores entre aspas. */
    private static void interpretar(String linha, Map<String, String> valores) {
        if (linha.startsWith("\uFEFF")) {
            linha = linha.substring(1); // marca invisível que o Bloco de Notas pode gravar no início
        }
        String texto = linha.trim();
        if (texto.isEmpty() || texto.startsWith("#")) {
            return;
        }
        if (texto.startsWith("export ")) {
            texto = texto.substring(7).trim();
        }
        int igual = texto.indexOf('=');
        if (igual <= 0) {
            return;
        }
        String chave = texto.substring(0, igual).trim();
        String valor = texto.substring(igual + 1).trim();
        if (valor.length() >= 2
                && ((valor.startsWith("\"") && valor.endsWith("\""))
                || (valor.startsWith("'") && valor.endsWith("'")))) {
            valor = valor.substring(1, valor.length() - 1);
        }
        valores.put(chave, valor);
    }

    /** Onde procurar, em ordem: ao lado do executável, na pasta do usuário e na pasta atual. */
    private static List<Path> locais() {
        List<Path> locais = new ArrayList<>();

        String executavel = System.getProperty("jpackage.app-path"); // definida pelo jpackage
        if (executavel != null && !executavel.isBlank()) {
            Path pasta = Path.of(executavel).getParent();
            if (pasta != null) {
                locais.add(pasta.resolve(ARQUIVO));
            }
        }
        locais.add(Path.of(System.getProperty("user.home"), ".chat-ia", ARQUIVO));
        locais.add(Path.of(ARQUIVO)); // pasta atual: a raiz do projeto, ao rodar pelo IntelliJ
        return locais;
    }
}