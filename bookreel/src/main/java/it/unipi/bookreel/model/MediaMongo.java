package it.unipi.bookreel.model;

import it.unipi.bookreel.DTO.media.ReviewDto;
import it.unipi.bookreel.enumerator.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.neo4j.core.schema.GeneratedValue;

import java.util.ArrayList;
import java.util.List;

@Data
public abstract class MediaMongo {
    @Id
    @Field("_id")
    private String id;

    @NotBlank
    private String name;

    @NotBlank
    private int sumScores = 0;

    private int numScores = 0; //numero di recensioni

    @NotBlank
    private List<String> genres;

    @NotEmpty
    private MediaType type;

    @NotEmpty
    private String synopsis;

    private List<ReviewDto> reviews = new ArrayList<>();
}