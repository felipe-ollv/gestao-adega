package com.adega.dto;

import com.adega.model.TipoMedidaVenda;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record AtualizarItemRequest(
        @NotNull UUID produtoUuid,
        @Positive int quantidade,
        @NotNull TipoMedidaVenda tipoMedida
) {
}
