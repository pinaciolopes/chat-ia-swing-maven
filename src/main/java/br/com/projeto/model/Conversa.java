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
public class Conversa {

    @BsonId
    private ObjectId id;

    private ObjectId ownerId;

    private String title;

    private Instant createdAt;

    private Instant updatedAt;
}