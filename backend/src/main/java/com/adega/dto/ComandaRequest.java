package com.adega.dto;

import jakarta.validation.constraints.NotBlank;

public record ComandaRequest(@NotBlank String nomeResponsavel) {
}
