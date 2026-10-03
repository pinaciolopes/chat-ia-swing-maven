package br.com.projeto.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mensagem {

    public static final String PAPEL_USUARIO = "user";
    public static final String PAPEL_ASSISTENTE = "assistant";

    @BsonId
    private ObjectId id;

    private ObjectId userId;

    private ObjectId conversationId;

    private String role;

    private String content;

    private Instant createdAt;
}