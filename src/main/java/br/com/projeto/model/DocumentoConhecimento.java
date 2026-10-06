package br.com.projeto.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.types.ObjectId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoConhecimento {

    @BsonId
    private ObjectId id;

    private String title;

    private String content;

    private List<String> keywords = new ArrayList<>();

    private Instant createdAt;

    private Instant updatedAt;
}