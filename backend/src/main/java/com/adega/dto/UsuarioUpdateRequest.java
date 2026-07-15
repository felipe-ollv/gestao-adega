package com.adega.dto;

import com.adega.model.PerfilUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(
        @NotBlank String nome,
        @NotBlank @Email String email,
        @Size(min = 8) String senha,
        @NotNull PerfilUsuario perfil,
        @NotNull Boolean ativo
) {
}
