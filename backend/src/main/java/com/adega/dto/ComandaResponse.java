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
        Instant dataExclusao,
        StatusComanda status,
        List<ComandaItemResponse> itens,
        BigDecimal total,
        BigDecimal valorPagoParcial,
        BigDecimal saldoPendente,
        String observacaoExclusao
) {
    public static ComandaResponse from(Comanda comanda) {
        List<ComandaItemResponse> itens = comanda.itens.stream()
                .map(ComandaItemResponse::from)
                .toList();
        BigDecimal total = itens.stream()
                .map(ComandaItemResponse::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal valorPagoParcial = comanda.valorPagoParcial == null ? BigDecimal.ZERO : comanda.valorPagoParcial;
        BigDecimal saldoPendente = total.subtract(valorPagoParcial);
        return new ComandaResponse(
                comanda.uuid,
                comanda.nomeResponsavel,
                comanda.dataAbertura,
                comanda.dataFechamento,
                comanda.dataExclusao,
                comanda.status,
                itens,
                total,
                valorPagoParcial,
                saldoPendente,
                comanda.observacaoExclusao
        );
    }
}
