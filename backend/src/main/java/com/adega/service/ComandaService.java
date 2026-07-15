package com.adega.service;

import com.adega.dto.AdicionarItemRequest;
import com.adega.dto.ComandaRequest;
import com.adega.dto.ComandaResponse;
import com.adega.dto.FecharComandaRequest;
import com.adega.exception.BusinessException;
import com.adega.exception.ForbiddenOperationException;
import com.adega.model.Adega;
import com.adega.model.Comanda;
import com.adega.model.ComandaItem;
import com.adega.model.Produto;
import com.adega.model.StatusComanda;
import com.adega.model.TipoMedidaVenda;
import com.adega.repository.AdegaRepository;
import com.adega.repository.ComandaItemRepository;
import com.adega.repository.ComandaRepository;
import com.adega.repository.ProdutoRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class ComandaService {
    @Inject
    ComandaRepository comandaRepository;

    @Inject
    ComandaItemRepository comandaItemRepository;

    @Inject
    ProdutoRepository produtoRepository;

    @Inject
    AdegaRepository adegaRepository;

    @Inject
    SecurityService securityService;

    public List<ComandaResponse> list(StatusComanda status) {
        return comandaRepository.listByAdega(securityService.currentAdegaUuid(), status)
                .stream()
                .map(ComandaResponse::from)
                .toList();
    }

    public ComandaResponse get(UUID uuid) {
        return ComandaResponse.from(findCurrentAdegaComanda(uuid));
    }

    @Transactional
    public ComandaResponse open(ComandaRequest request) {
        Adega adega = adegaRepository.findByUuid(securityService.currentAdegaUuid())
                .orElseThrow(() -> new BusinessException("Adega não encontrada."));

        Comanda comanda = new Comanda();
        comanda.adega = adega;
        comanda.nomeResponsavel = request.nomeResponsavel().trim();
        comanda.status = StatusComanda.ABERTA;
        comandaRepository.persist(comanda);

        return ComandaResponse.from(comanda);
    }

    @Transactional
    public ComandaResponse addItem(UUID comandaUuid, AdicionarItemRequest request) {
        Comanda comanda = findCurrentAdegaComanda(comandaUuid);
        if (comanda.status != StatusComanda.ABERTA) {
            throw new BusinessException("Itens só podem ser adicionados em comandas abertas.");
        }

        Produto produto = produtoRepository.findByUuidAndAdega(request.produtoUuid(), securityService.currentAdegaUuid())
                .filter(candidate -> candidate.ativo)
                .orElseThrow(() -> new BusinessException("Produto não encontrado."));

        int unidadesParaDeduzir = request.quantidade();
        BigDecimal valorAplicado = produto.valorUnidade;

        if (request.tipoMedida() == TipoMedidaVenda.CAIXA) {
            if (produto.valorCaixa == null) {
                throw new BusinessException("Produto não configurado para venda por caixa.");
            }
            unidadesParaDeduzir = request.quantidade() * produto.unidadesPorCaixa;
            valorAplicado = produto.valorCaixa;
        }

        if (produto.quantidadeEstoqueUnidades < unidadesParaDeduzir) {
            throw new BusinessException("Estoque insuficiente para o produto: " + produto.nome);
        }

        produto.quantidadeEstoqueUnidades -= unidadesParaDeduzir;

        ComandaItem item = new ComandaItem();
        item.comanda = comanda;
        item.produto = produto;
        item.quantidadePedida = request.quantidade();
        item.unidadesDeduzidas = unidadesParaDeduzir;
        item.tipoMedidaVendida = request.tipoMedida();
        item.valorCobradoUnitario = valorAplicado;
        comandaItemRepository.persist(item);
        comanda.itens.add(item);

        return ComandaResponse.from(comanda);
    }

    @Transactional
    public ComandaResponse close(UUID uuid, FecharComandaRequest request) {
        if (request.status() == StatusComanda.ABERTA) {
            throw new BusinessException("Use PAGA ou FIADO para fechar a comanda.");
        }
        if (request.status() == StatusComanda.FIADO && !securityService.isGestor()) {
            throw new ForbiddenOperationException("Apenas gestores podem fechar comandas como fiado.");
        }

        Comanda comanda = findCurrentAdegaComanda(uuid);
        if (request.status() == StatusComanda.FIADO && comanda.status != StatusComanda.ABERTA) {
            throw new BusinessException("Apenas comandas abertas podem ser fechadas como fiado.");
        }
        if (request.status() == StatusComanda.PAGA
                && comanda.status != StatusComanda.ABERTA
                && comanda.status != StatusComanda.FIADO) {
            throw new BusinessException("Comanda já está paga.");
        }

        comanda.status = request.status();
        comanda.dataFechamento = Instant.now();
        return ComandaResponse.from(comanda);
    }

    private Comanda findCurrentAdegaComanda(UUID uuid) {
        return comandaRepository.findByUuidAndAdega(uuid, securityService.currentAdegaUuid())
                .orElseThrow(() -> new BusinessException("Comanda não encontrada."));
    }
}
