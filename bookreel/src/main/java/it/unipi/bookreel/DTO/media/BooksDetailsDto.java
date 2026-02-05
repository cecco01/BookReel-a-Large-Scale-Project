package it.unipi.bookreel.DTO.media;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class BooksDetailsDto extends MediaDetailsDto {
    private int chapters;

    private List<String> authors;
}


//nel caso mettere anche il genere e altri sttributi specifici dei libri