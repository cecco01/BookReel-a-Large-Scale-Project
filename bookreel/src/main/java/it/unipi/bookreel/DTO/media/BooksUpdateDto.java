package it.unipi.bookreel.DTO.media;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BooksUpdateDto extends MediaUpdateDto {
    @NotEmpty
    private Integer numPages;

    @NotEmpty
    private List<String> authors;

    @NotEmpty
    private List<String> publishers;
}


//nel caso mettere anche il genere e altri sttributi specifici dei libri