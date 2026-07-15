package com.adega.dto;

import com.adega.model.StatusComanda;
import jakarta.validation.constraints.NotNull;

public record FecharComandaRequest(@NotNull StatusComanda status) {
}
