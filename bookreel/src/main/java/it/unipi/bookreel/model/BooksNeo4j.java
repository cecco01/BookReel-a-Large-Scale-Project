package it.unipi.bookreel.model;

import it.unipi.bookreel.enumerator.MediaStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;

@Node("Books")
@Data
public class BooksNeo4j {

    @Id
    private String id;

    @Property("name")
    private String name;

    @Property("status")
    private MediaStatus status;

    @Property("genres")
    private List<String> genres;
}