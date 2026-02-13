package it.unipi.bookreel.DTO.user;

import it.unipi.bookreel.enumerator.PrivacyStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserNoPwdDto(
        @NotBlank String username,
        @NotBlank @Email String email,
        PrivacyStatus privacyStatus) {
}