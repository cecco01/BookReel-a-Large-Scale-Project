package it.unipi.bookreel.DTO.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserLoginDto {
    @NotBlank
    @Size(min = 4, message = "La password deve avere almeno 4 caratteri")
    private String password;
    @NotBlank
    @Email
    private String email;
}