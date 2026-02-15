package it.unipi.bookreel.DTO.analytic;

import java.util.List;
import lombok.Data;

@Data
public class ListCounterAnalyticDto {
    private String listType;          // "PLANNED" o "COMPLETED"
    private List<TopMediaDto> topMedia; // Lista dei media più popolari
}