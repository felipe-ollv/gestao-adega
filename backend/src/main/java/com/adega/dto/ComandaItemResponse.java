package com.adega.dto;

import com.adega.model.ComandaItem;
import com.adega.model.TipoMedidaVenda;
import java.math.BigDecimal;
import java.util.UUID;

public record ComandaItemResponse(
        UUID uuid,
        UUID produtoUuid,
        String produtoNome,
        int quantidadePedida,
        int unidadesDeduzidas,
        TipoMedidaVenda tipoMedida,
        BigDecimal valorUnitario,
        BigDecimal subtotal,
        UUID grupoUuid,
        Integer ordemGrupo
) {
    public static ComandaItemResponse from(ComandaItem item) {
        BigDecimal subtotal = item.valorCobradoUnitario.multiply(BigDecimal.valueOf(item.quantidadePedida));
        return new ComandaItemResponse(
                item.uuid,
                item.produto.uuid,
                item.produto.nome,
                item.quantidadePedida,
                item.unidadesDeduzidas,
                item.tipoMedidaVendida,
                item.valorCobradoUnitario,
                subtotal,
                item.grupoUuid,
                item.ordemGrupo
        );
    }
}
