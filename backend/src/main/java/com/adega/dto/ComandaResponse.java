package com.adega.dto;

import com.adega.model.Comanda;
import com.adega.model.StatusComanda;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ComandaResponse(
        UUID uuid,
        String nomeResponsavel,
        Instant dataAbertura,
        Instant dataFechamento,
        StatusComanda status,
        List<ComandaItemResponse> itens,
        BigDecimal total
) {
    public static ComandaResponse from(Comanda comanda) {
        List<ComandaItemResponse> itens = comanda.itens.stream()
                .map(ComandaItemResponse::from)
                .toList();
        BigDecimal total = itens.stream()
                .map(ComandaItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new ComandaResponse(
                comanda.uuid,
                comanda.nomeResponsavel,
                comanda.dataAbertura,
                comanda.dataFechamento,
                comanda.status,
                itens,
                total
        );
    }
}
