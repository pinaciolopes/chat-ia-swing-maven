package br.com.projeto.repository;

import br.com.projeto.config.MongoConfig;
import br.com.projeto.model.Conversa;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;

public class ConversaRepository {

    private final MongoCollection<Conversa> colecao;

    public ConversaRepository() {
        colecao = MongoConfig.getDatabase().getCollection("conversations", Conversa.class);
        colecao.createIndex(Indexes.ascending("ownerId"));
    }

    private Bson porIdEDono(ObjectId id, ObjectId ownerId) {
        return Filters.and(Filters.eq("_id", id), Filters.eq("ownerId", ownerId));
    }

    public Conversa inserir(Conversa conversa) {
        colecao.insertOne(conversa);
        return conversa;
    }

    public List<Conversa> listarPorDono(ObjectId ownerId) {
        return colecao.find(Filters.eq("ownerId", ownerId))
                .sort(Sorts.descending("updatedAt"))
                .into(new ArrayList<>());
    }

    public Optional<Conversa> buscarPorIdEDono(ObjectId id, ObjectId ownerId) {
        return Optional.ofNullable(colecao.find(porIdEDono(id, ownerId)).first());
    }

    public boolean renomear(ObjectId id, ObjectId ownerId, String novoTitulo) {
        return colecao.updateOne(porIdEDono(id, ownerId),
                        Updates.combine(
                                Updates.set("title", novoTitulo),
                                Updates.set("updatedAt", Instant.now())))
                .getMatchedCount() > 0;
    }

    public boolean atualizarData(ObjectId id, ObjectId ownerId) {
        return colecao.updateOne(porIdEDono(id, ownerId), Updates.set("updatedAt", Instant.now()))
                .getMatchedCount() > 0;
    }
    public boolean excluir(ObjectId id, ObjectId ownerId) {
        return colecao.deleteOne(porIdEDono(id, ownerId)).getDeletedCount() > 0;
    }
}