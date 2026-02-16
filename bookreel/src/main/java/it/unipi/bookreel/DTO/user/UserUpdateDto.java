package it.unipi.bookreel.DTO.user;

import it.unipi.bookreel.enumerator.PrivacyStatus;
import jakarta.validation.constraints.*;

public record UserUpdateDto(
        @NotEmpty
        String username,
        @Size(min = 4, message = "La password deve avere almeno 4 caratteri")
        String password,
        @Email
        String email,
        PrivacyStatus privacyStatus
){}