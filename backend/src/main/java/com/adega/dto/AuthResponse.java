package com.adega.dto;

import com.adega.model.PerfilUsuario;
import com.adega.model.StatusPagamento;
import java.time.LocalDate;
import java.util.UUID;

public record AuthResponse(
        String token,
        UUID usuarioUuid,
        UUID adegaUuid,
        String adegaNome,
        String nome,
        PerfilUsuario perfil,
        StatusPagamento statusPagamento,
        boolean mensalidadePaga,
        LocalDate competenciaMensalidade,
        LocalDate vencimentoMensalidade
) {
}
