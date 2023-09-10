package com.ycash.server.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record AuthenticationRequestDTO(
        @NotBlank(message = "E-mail não pode estar nulo ou vazio.") @Email(message = "E-mail inválido. Por favor, informe um endereço de e-mail válido.") String email,
        @NotBlank(message = "Senha não pode estar nula ou vazia.") String password
) {
}
