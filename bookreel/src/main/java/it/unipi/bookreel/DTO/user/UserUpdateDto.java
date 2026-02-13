package it.unipi.bookreel.DTO.user;

import it.unipi.bookreel.enumerator.PrivacyStatus;
import jakarta.validation.constraints.*;

public record UserUpdateDto(
        @NotEmpty
        String username,
        @Min(4)
        String password,
        @Email
        String email,
        PrivacyStatus privacyStatus
){}