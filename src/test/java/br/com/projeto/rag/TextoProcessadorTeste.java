package br.com.projeto.rag;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TextoProcessadorTeste {

    @Test
    void normalizaMinusculasEAcentos() {
        assertEquals("programacao orientada a objetos",
                TextoProcessador.normalizar("Programação Orientada a Objetos"));
    }

    @Test
    void textoNuloViraVazio() {
        assertEquals("", TextoProcessador.normalizar(null));
        assertTrue(TextoProcessador.extrairPalavrasChave(null).isEmpty());
    }

    @Test
    void removeStopWordsDaPergunta() {
        List<String> palavras = TextoProcessador.extrairPalavrasChave("Explique o encapsulamento em Java para mim");

        assertTrue(palavras.contains("explique"));
        assertTrue(palavras.contains("encapsulamento"));
        assertTrue(palavras.contains("java"));
        assertFalse(palavras.contains("o"));
        assertFalse(palavras.contains("em"));
        assertFalse(palavras.contains("para"));
    }

    @Test
    void acentoNaoImpedeABusca() {
        assertEquals(List.of("heranca", "polimorfismo"),
                TextoProcessador.extrairPalavrasChave("Herança e polimorfismo"));
    }

    @Test
    void textoSoComStopWordsNaoGeraPalavrasChave() {
        assertTrue(TextoProcessador.extrairPalavrasChave("o de a em para que").isEmpty());
    }

    @Test
    void naoRepetePalavras() {
        assertEquals(List.of("java"), TextoProcessador.extrairPalavrasChave("Java java JAVA"));
    }

    @Test
    void reconheceStopWordsComEPalavrasSemAcento() {
        assertTrue(TextoProcessador.ehStopWord("DE"));
        assertTrue(TextoProcessador.ehStopWord("são"));
        assertFalse(TextoProcessador.ehStopWord("java"));
    }
}