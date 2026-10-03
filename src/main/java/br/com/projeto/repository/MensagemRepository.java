package br.com.projeto.repository;

import br.com.projeto.config.MongoConfig;
import br.com.projeto.model.Mensagem;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import java.util.ArrayList;
import java.util.List;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class MensagemRepository {

    private final MongoCollection<Mensagem> colecao;

    public MensagemRepository() {
        colecao = MongoConfig.getDatabase().getCollection("messages", Mensagem.class);
        colecao.createIndex(Indexes.ascending("conversationId"));
    }

    private Bson porConversaEUsuario(ObjectId conversationId, ObjectId userId) {
        return Filters.and(
                Filters.eq("conversationId", conversationId),
                Filters.eq("userId", userId));
    }

    public Mensagem inserir(Mensagem mensagem) {
        colecao.insertOne(mensagem);
        return mensagem;
    }

    public List<Mensagem> listarPorConversa(ObjectId conversationId, ObjectId userId) {
        return colecao.find(porConversaEUsuario(conversationId, userId))
                .sort(Sorts.ascending("createdAt", "_id"))
                .into(new ArrayList<>());
    }

    public long excluirPorConversa(ObjectId conversationId, ObjectId userId) {
        return colecao.deleteMany(porConversaEUsuario(conversationId, userId)).getDeletedCount();
    }
}