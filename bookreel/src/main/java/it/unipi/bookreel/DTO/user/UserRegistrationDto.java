package it.unipi.bookreel.DTO.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRegistrationDto(
        @NotBlank
        String username,
        @NotBlank
        @Size(min = 4, message = "La password deve avere almeno 4 caratteri")
        String password,
        @NotBlank
        @Email
        String email
) {
}