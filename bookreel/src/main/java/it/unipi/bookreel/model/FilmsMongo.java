package it.unipi.bookreel.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "films")
@Data
@EqualsAndHashCode(callSuper = true)
public class FilmsMongo extends MediaMongo{
    @NotBlank

    private String source;

    private double duration;

    private List<String> studios;
}