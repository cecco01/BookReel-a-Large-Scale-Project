package it.unipi.bookreel.DTO.media;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.unipi.bookreel.enumerator.MediaStatus;
import it.unipi.bookreel.enumerator.MediaType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "mediaType")
@JsonSubTypes({
        @JsonSubTypes.Type(value = FilmsUpdateDto.class, name = "Films"),
        @JsonSubTypes.Type(value = BooksUpdateDto.class, name = "Books")
})
@Data
public abstract class MediaUpdateDto {
    @NotBlank
    private MediaType mediaType;

    @NotEmpty
    private String name;

    @NotEmpty
    private MediaStatus status;

    @NotEmpty
    private List<String> genres;

    @NotEmpty
    private String synopsis;

    @NotEmpty
    private String type;
}