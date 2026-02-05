package it.unipi.bookreel.DTO.media;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BooksCreationDto extends MediaCreationDto {
    @NotBlank
    private int chapters;

    @NotEmpty
    private List<String> authors;
}

//nel caso mettere anche il genere e altri sttributi specifici dei libri