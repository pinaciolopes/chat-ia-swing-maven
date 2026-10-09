package br.com.projeto.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TokenRecuperacaoSenha {

    @BsonId
    private ObjectId id;

    private ObjectId userId;

    @ToString.Exclude
    private String tokenHash;

    private Instant expiresAt;

    private boolean used;
}