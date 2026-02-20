package it.unipi.bookreel.DTO.analytic;

import lombok.Data;

@Data
public class TrendingMediaDto {
    private String id;
    private String name;
    private Double scoreDifference;
}