package br.com.projeto.repository;

import br.com.projeto.config.MongoConfig;
import br.com.projeto.model.Usuario;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Updates;
import java.util.Optional;
import org.bson.types.ObjectId;

public class UsuarioRepository {

    private final MongoCollection<Usuario> colecao;

    public UsuarioRepository() {
        colecao = MongoConfig.getDatabase().getCollection("users", Usuario.class);
        colecao.createIndex(Indexes.ascending("email"), new IndexOptions().unique(true));
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return Optional.ofNullable(colecao.find(Filters.eq("email", email)).first());
    }

    public Optional<Usuario> buscarPorId(ObjectId id) {
        return Optional.ofNullable(colecao.find(Filters.eq("_id", id)).first());
    }

    public Usuario inserir(Usuario usuario) {
        colecao.insertOne(usuario);
        return usuario;
    }

    public void atualizarSenhaHash(ObjectId usuarioId, String novoHash) {
        colecao.updateOne(Filters.eq("_id", usuarioId), Updates.set("passwordHash", novoHash));
    }
}
