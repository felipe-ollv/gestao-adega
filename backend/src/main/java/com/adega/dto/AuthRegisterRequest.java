package com.adega.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AuthRegisterRequest(
        @NotBlank String nomeAdega,
        @NotBlank @Size(max = 18) String cnpjCpf,
        @NotBlank String nomeUsuario,
        @NotBlank @Email String email,
        @NotBlank @Size(min = 8) String senha
) {
}
