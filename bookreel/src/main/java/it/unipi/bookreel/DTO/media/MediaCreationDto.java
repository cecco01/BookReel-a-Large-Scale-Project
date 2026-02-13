package it.unipi.bookreel.DTO.media;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.unipi.bookreel.enumerator.MediaType;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "mediaType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FilmsCreationDto.class, name = "Films"),
        @JsonSubTypes.Type(value = BooksCreationDto.class, name = "Books")
})
@Data
public abstract class MediaCreationDto {
    @NotBlank
    private MediaType type;

    @NotBlank
    private String name;

    @NotBlank
    private List<String> genres;

    private String synopsis;
}