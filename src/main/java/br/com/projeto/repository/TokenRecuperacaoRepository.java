package br.com.projeto.repository;

import br.com.projeto.config.MongoConfig;
import br.com.projeto.model.TokenRecuperacaoSenha;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import java.time.Instant;
import java.util.Optional;
import org.bson.types.ObjectId;

public class TokenRecuperacaoRepository {

    private final MongoCollection<TokenRecuperacaoSenha> colecao;

    public TokenRecuperacaoRepository() {
        colecao = MongoConfig.getDatabase().getCollection("password_reset_tokens", TokenRecuperacaoSenha.class);
        colecao.createIndex(Indexes.ascending("tokenHash"));
    }

    public TokenRecuperacaoSenha inserir(TokenRecuperacaoSenha token) {
        colecao.insertOne(token);
        return token;
    }

    /** Só devolve o token se ele ainda não foi usado E ainda não expirou. */
    public Optional<TokenRecuperacaoSenha> buscarValidoPorHash(String tokenHash) {
        return Optional.ofNullable(colecao.find(Filters.and(
                Filters.eq("tokenHash", tokenHash),
                Filters.eq("used", false),
                Filters.gt("expiresAt", Instant.now()))).first());
    }

    public void marcarComoUsado(ObjectId id) {
        colecao.updateOne(Filters.eq("_id", id), Updates.set("used", true));
    }

    /** Invalida todos os tokens ainda não usados do usuário e devolve quantos eram. */
    public long invalidarPendentes(ObjectId userId) {
        return colecao.updateMany(
                Filters.and(Filters.eq("userId", userId), Filters.eq("used", false)),
                Updates.set("used", true)).getModifiedCount();
    }

    public boolean excluir(ObjectId id) {
        return colecao.deleteOne(Filters.eq("_id", id)).getDeletedCount() > 0;
    }
}