package com.adega.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ExcluirComandaRequest(
        @NotBlank(message = "Informe o motivo da exclusão da comanda.")
        @Size(max = 500, message = "A observação da exclusão deve ter no máximo 500 caracteres.")
        String observacao
) {
}
