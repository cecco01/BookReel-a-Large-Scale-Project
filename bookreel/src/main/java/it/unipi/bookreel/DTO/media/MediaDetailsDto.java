package it.unipi.bookreel.DTO.media;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import it.unipi.bookreel.enumerator.MediaType;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public abstract class MediaDetailsDto {
    private String name;

    private double avgScore;

    private List<String> genres;

    private String synopsis;

    private MediaType type;

    private List<ReviewDto> reviews;
}