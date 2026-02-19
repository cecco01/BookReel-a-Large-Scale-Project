package it.unipi.bookreel.DTO.analytic;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TrendingMediaDto {
    @NotBlank
    private String id;
    @NotBlank
    private String name;
    @NotBlank
    private float scoreDifference;
}