package it.unipi.bookreel.DTO.media;

import it.unipi.bookreel.DTO.analytic.AppearancesDto;
import lombok.Data;

import java.util.List;

@Data
public class MediaInListsAnalyticDto {
    private String mediaId;
    private String mediaName;
    private List<AppearancesDto> appearances;
}