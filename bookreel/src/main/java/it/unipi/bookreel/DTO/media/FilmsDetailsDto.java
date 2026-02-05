package it.unipi.bookreel.DTO.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class FilmsDetailsDto extends MediaDetailsDto {

    @NotEmpty
    private String source;

    @NotEmpty
    private double duration;

    @NotEmpty
    private List<String> studios;
}

//vedi se occorre aggiungere altri attributi specifici dei film