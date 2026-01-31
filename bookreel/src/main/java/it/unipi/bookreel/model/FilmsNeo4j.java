package it.unipi.bookreel.model;

import it.unipi.bookreel.enumerator.MediaStatus;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.util.List;

@Node("Films")
@Data
public class AnimeNeo4j {

    @Id
    private String id;

    @Property("name")
    private String name;

    @Property("genres")
    private List<String> genres;
}