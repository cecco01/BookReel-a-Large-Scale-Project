package it.unipi.bookreel.DTO.media;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Getter
@SuperBuilder
public class BooksDetailsDto extends MediaDetailsDto {
    private int numPages;

    private List<String> authors;

    private List<String> publishers;
}
