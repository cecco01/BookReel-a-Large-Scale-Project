package it.unipi.bookreel.DTO.user;

import it.unipi.bookreel.enumerator.PrivacyStatus;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.Date;

public record UserUpdateDto(
        @NotEmpty
        String username,
        @Min(4)
        String password,
        @Email
        String email,
        @Past
        Date birthdate,
        PrivacyStatus privacyStatus
){}