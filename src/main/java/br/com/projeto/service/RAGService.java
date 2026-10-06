package br.com.projeto.service;

import br.com.projeto.model.DocumentoConhecimento;
import br.com.projeto.rag.TextoProcessador;
import br.com.projeto.repository.DocumentoRepository;
import br.com.projeto.util.AppException;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RAGService {

    private static final int MAXIMO_DOCUMENTOS = 3;

    private final DocumentoRepository documentoRepository = new DocumentoRepository();

    public DocumentoConhecimento cadastrarDocumento(String titulo, String conteudo) {
        if (titulo == null || titulo.isBlank() || conteudo == null || conteudo.isBlank()) {
            throw new AppException("Informe título e conteúdo do documento.");
        }

        DocumentoConhecimento documento = new DocumentoConhecimento();
        documento.setTitle(titulo.trim());
        documento.setContent(conteudo.trim());
        documento.setKeywords(TextoProcessador.extrairPalavrasChave(titulo + " " + conteudo));
        documento.setCreatedAt(Instant.now());
        documento.setUpdatedAt(Instant.now());
        return documentoRepository.inserir(documento);
    }


    public List<DocumentoConhecimento> buscar(String pergunta) {
        List<String> palavras = TextoProcessador.extrairPalavrasChave(pergunta);
        if (palavras.isEmpty()) {
            return List.of();
        }

        return documentoRepository.buscarPorPalavrasChave(palavras).stream()
                .sorted(Comparator.comparingLong((DocumentoConhecimento d) -> coincidencias(d, palavras)).reversed())
                .limit(MAXIMO_DOCUMENTOS)
                .toList();
    }

    public String recuperarContexto(String pergunta) {
        return buscar(pergunta).stream()
                .map(d -> "### " + d.getTitle() + "\n" + d.getContent())
                .collect(Collectors.joining("\n\n"));
    }

    private long coincidencias(DocumentoConhecimento documento, List<String> palavras) {
        if (documento.getKeywords() == null) {
            return 0;
        }
        return documento.getKeywords().stream().filter(palavras::contains).count();
    }
}