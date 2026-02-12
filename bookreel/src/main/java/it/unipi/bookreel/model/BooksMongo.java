package it.unipi.bookreel.model;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(collection = "books")
@Data
@EqualsAndHashCode(callSuper = true)
public class BooksMongo extends MediaMongo {
    @NotBlank
    private int numPages;

    private List<String> authors;

    private List<String> publishers;
}