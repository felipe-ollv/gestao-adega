package com.adega.dto;

import com.adega.model.PerfilUsuario;
import java.util.UUID;

public record AuthResponse(
        String token,
        UUID usuarioUuid,
        UUID adegaUuid,
        String nome,
        PerfilUsuario perfil
) {
}
