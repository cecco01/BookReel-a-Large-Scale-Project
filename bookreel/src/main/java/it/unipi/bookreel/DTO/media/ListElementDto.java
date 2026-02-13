package it.unipi.bookreel.DTO.media;

import lombok.Data;

@Data
public class ListElementDto {
    private String id;
    private String name;
    private int progress;
    private int total;
}