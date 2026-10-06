package br.com.projeto.rag;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


public final class TextoProcessador {

    private static final Set<String> STOP_WORDS = Set.of(
            "a", "o", "as", "os", "de", "da", "do", "das", "dos", "em", "no", "na", "nos", "nas",
            "para", "por", "com", "uma", "um", "uns", "umas", "que", "e", "ou", "se", "ao", "aos",
            "como", "mais", "mas", "foi", "ser", "sao", "qual", "quais", "sobre", "entre",
            "eu", "voce", "me", "te", "meu", "minha", "seu", "sua", "este", "esta", "isso", "isto");

    private static final int TAMANHO_MINIMO_PALAVRA = 3;

    private TextoProcessador() {
    }

    public static String normalizar(String texto) {
        if (texto == null) {
            return "";
        }
        String decomposto = Normalizer.normalize(texto.toLowerCase(), Normalizer.Form.NFD);
        return decomposto.replaceAll("\\p{M}", "");
    }

    /** Separa o texto em palavras, descartando pontuação e espaços. */
    public static List<String> tokenizar(String texto) {
        String normalizado = normalizar(texto).trim();
        if (normalizado.isEmpty()) {
            return List.of();
        }
        return Arrays.stream(normalizado.split("[^a-z0-9]+"))
                .filter(token -> !token.isEmpty())
                .toList();
    }

    public static List<String> removerStopWords(List<String> tokens) {
        return tokens.stream()
                .filter(token -> !STOP_WORDS.contains(token))
                .toList();
    }

    /** Fluxo completo: normaliza, tokeniza, remove stop words e palavras muito curtas, sem repetições. */
    public static List<String> extrairPalavrasChave(String texto) {
        return removerStopWords(tokenizar(texto)).stream()
                .filter(palavra -> palavra.length() >= TAMANHO_MINIMO_PALAVRA)
                .distinct()
                .toList();
    }

    public static boolean ehStopWord(String palavra) {
        return STOP_WORDS.contains(normalizar(palavra));
    }
}