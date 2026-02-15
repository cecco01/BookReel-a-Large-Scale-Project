package it.unipi.bookreel.DTO.analytic;

import lombok.Data;

import java.util.List;

@Data
public class AppearancesDto {
    private String listType;          // "PLANNED" o "COMPLETED"
    private int listCount;
}