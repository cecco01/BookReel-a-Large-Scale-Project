package it.unipi.bookreel.DTO.analytic;

import it.unipi.bookreel.DTO.user.UserIdUsernameDto;
import lombok.Data;

import java.util.List;
// Strongly Connected Components
@Data
public class SCCAnalyticDto {
    private int componentId;
    private int componentSize;
    List<UserIdUsernameDto> userDetails;
    
    public SCCAnalyticDto(int componentId, int componentSize, List<UserIdUsernameDto> userDetails) {
        this.componentId = componentId;
        this.componentSize = componentSize;
        this.userDetails = userDetails;
    }
}