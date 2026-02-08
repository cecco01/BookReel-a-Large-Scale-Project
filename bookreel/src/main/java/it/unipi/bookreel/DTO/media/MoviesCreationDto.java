package it.unipi.bookreel.DTO.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MoviesCreationDto extends MediaCreationDto {

    @NotEmpty
    private String source;

    @NotEmpty
    private double duration;

    @NotEmpty
    private List<String> studios;
}

//vedi se occorre aggiungere altri attributi specifici dei film