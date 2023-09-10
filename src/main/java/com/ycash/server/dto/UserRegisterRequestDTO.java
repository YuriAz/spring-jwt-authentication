package com.ycash.server.dto;

import com.ycash.server.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRegisterRequestDTO(
        @NotBlank(message = "Nome não pode estar nulo ou vazio.") String firstName,
        @NotBlank(message = "Nome não pode estar nulo ou vazio.") String lastName,
        @NotBlank(message = "E-mail não pode estar nulo ou vazio.") @Email(message = "E-mail inválido. Por favor, informe um endereço de e-mail válido.") String email,
        @NotBlank(message = "Senha não pode estar nula ou vazia.") String password,
        @NotNull(message = "Role não pode estar nula.") Role role,
        @NotNull boolean mfaEnabled
) {
}
