package br.com.projeto.repository;

import br.com.projeto.config.MongoConfig;
import br.com.projeto.model.DocumentoConhecimento;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bson.types.ObjectId;

public class DocumentoRepository {

    private final MongoCollection<DocumentoConhecimento> colecao;

    public DocumentoRepository() {
        colecao = MongoConfig.getDatabase().getCollection("documents", DocumentoConhecimento.class);
        colecao.createIndex(Indexes.ascending("keywords"));
    }

    public DocumentoConhecimento inserir(DocumentoConhecimento documento) {
        colecao.insertOne(documento);
        return documento;
    }

    public List<DocumentoConhecimento> buscarPorPalavrasChave(Collection<String> palavras) {
        if (palavras == null || palavras.isEmpty()) {
            return List.of();
        }
        return colecao.find(Filters.in("keywords", palavras)).into(new ArrayList<>());
    }

    public List<DocumentoConhecimento> listarTodos() {
        return colecao.find().sort(Sorts.ascending("title")).into(new ArrayList<>());
    }

    public boolean excluir(ObjectId id) {
        return colecao.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
    }
}