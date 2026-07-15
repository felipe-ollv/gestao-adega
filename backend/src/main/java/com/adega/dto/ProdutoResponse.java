package com.adega.dto;

import com.adega.model.Produto;
import java.math.BigDecimal;
import java.util.UUID;

public record ProdutoResponse(
        UUID uuid,
        String nome,
        int quantidadeEstoqueUnidades,
        int unidadesPorCaixa,
        BigDecimal valorUnidade,
        BigDecimal valorCaixa
) {
    public static ProdutoResponse from(Produto produto) {
        return new ProdutoResponse(
                produto.uuid,
                produto.nome,
                produto.quantidadeEstoqueUnidades,
                produto.unidadesPorCaixa,
                produto.valorUnidade,
                produto.valorCaixa
        );
    }
}
