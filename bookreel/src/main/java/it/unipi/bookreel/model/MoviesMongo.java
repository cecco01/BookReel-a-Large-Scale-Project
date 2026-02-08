package it.unipi.bookreel.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "movies")
@Data
@EqualsAndHashCode(callSuper = true)
public class MoviesMongo extends MediaMongo{
    @NotBlank

    private String director;

    private double duration;

    private List<String> studios;
}