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
public class Usuario {

    @BsonId
    private ObjectId id;

    private String name;

    private String email;

    @ToString.Exclude
    private String passwordHash;

    private Instant createdAt;
}
