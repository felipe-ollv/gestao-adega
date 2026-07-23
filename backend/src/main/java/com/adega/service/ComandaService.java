package com.adega.service;

import com.adega.dto.AdicionarItemRequest;
import com.adega.dto.AtualizarItemRequest;
import com.adega.dto.ComandaRequest;
import com.adega.dto.ComandaResponse;
import com.adega.dto.ExcluirComandaRequest;
import com.adega.dto.FecharComandaRequest;
import com.adega.dto.PagamentoParcialComandaRequest;
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

        ItemPricing pricing = pricingFor(produto, request.quantidade(), request.tipoMedida());
        deductStock(produto, pricing.unidadesParaDeduzir());

        ComandaItem item = new ComandaItem();
        item.comanda = comanda;
        item.produto = produto;
        item.quantidadePedida = request.quantidade();
        item.unidadesDeduzidas = pricing.unidadesParaDeduzir();
        item.tipoMedidaVendida = request.tipoMedida();
        item.valorCobradoUnitario = pricing.valorAplicado();
        comandaItemRepository.persist(item);
        comanda.itens.add(item);

        return ComandaResponse.from(comanda);
    }

    @Transactional
    public ComandaResponse updateItem(UUID comandaUuid, UUID itemUuid, AtualizarItemRequest request) {
        Comanda comanda = findCurrentAdegaComanda(comandaUuid);
        ensureOpen(comanda, "Itens só podem ser editados em comandas abertas.");

        ComandaItem item = findCurrentAdegaComandaItem(comandaUuid, itemUuid);
        Produto produto = produtoRepository.findByUuidAndAdega(request.produtoUuid(), securityService.currentAdegaUuid())
                .filter(candidate -> candidate.ativo)
                .orElseThrow(() -> new BusinessException("Produto não encontrado."));

        ItemPricing pricing = pricingFor(produto, request.quantidade(), request.tipoMedida());
        BigDecimal totalAtualizado = total(comanda)
                .subtract(subtotal(item))
                .add(pricing.valorAplicado().multiply(BigDecimal.valueOf(request.quantidade())));
        ensureTotalCoversPaid(comanda, totalAtualizado);

        item.produto.quantidadeEstoqueUnidades += item.unidadesDeduzidas;
        deductStock(produto, pricing.unidadesParaDeduzir());

        item.produto = produto;
        item.quantidadePedida = request.quantidade();
        item.unidadesDeduzidas = pricing.unidadesParaDeduzir();
        item.tipoMedidaVendida = request.tipoMedida();
        item.valorCobradoUnitario = pricing.valorAplicado();

        return ComandaResponse.from(comanda);
    }

    @Transactional
    public ComandaResponse deleteItem(UUID comandaUuid, UUID itemUuid) {
        Comanda comanda = findCurrentAdegaComanda(comandaUuid);
        ensureOpen(comanda, "Itens só podem ser excluídos em comandas abertas.");

        ComandaItem item = findCurrentAdegaComandaItem(comandaUuid, itemUuid);
        ensureTotalCoversPaid(comanda, total(comanda).subtract(subtotal(item)));

        item.produto.quantidadeEstoqueUnidades += item.unidadesDeduzidas;
        comanda.itens.remove(item);
        comandaItemRepository.delete(item);

        return ComandaResponse.from(comanda);
    }

    @Transactional
    public ComandaResponse close(UUID uuid, FecharComandaRequest request) {
        if (request.status() == StatusComanda.ABERTA) {
            throw new BusinessException("Use PAGA ou FIADO para fechar a comanda.");
        }
        if (request.status() == StatusComanda.EXCLUIDA) {
            throw new BusinessException("Use a ação de exclusão para excluir a comanda.");
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
        if (request.status() == StatusComanda.PAGA) {
            comanda.valorPagoParcial = total(comanda);
        }
        return ComandaResponse.from(comanda);
    }

    @Transactional
    public ComandaResponse payPartial(UUID uuid, PagamentoParcialComandaRequest request) {
        if (request == null || request.valor() == null || request.valor().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("O valor do pagamento parcial deve ser maior que zero.");
        }

        Comanda comanda = findCurrentAdegaComanda(uuid);
        ensureOpen(comanda, "Pagamentos parciais só podem ser lançados em comandas abertas.");

        BigDecimal total = total(comanda);
        if (total.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Adicione itens antes de lançar pagamento parcial.");
        }

        BigDecimal valorPagoAtual = paidValue(comanda);
        BigDecimal novoValorPago = valorPagoAtual.add(request.valor());
        if (novoValorPago.compareTo(total) > 0) {
            throw new BusinessException("O pagamento parcial não pode ultrapassar o total da comanda.");
        }

        comanda.valorPagoParcial = novoValorPago;
        return ComandaResponse.from(comanda);
    }

    @Transactional
    public void delete(UUID uuid, ExcluirComandaRequest request) {
        if (request == null || request.observacao() == null || request.observacao().trim().isBlank()) {
            throw new BusinessException("Informe o motivo da exclusão da comanda.");
        }

        Comanda comanda = findCurrentAdegaComanda(uuid);
        if (comanda.status == StatusComanda.EXCLUIDA) {
            throw new BusinessException("Comanda já excluída.");
        }
        if (comanda.status == StatusComanda.PAGA) {
            throw new BusinessException("Comandas pagas não podem ser excluídas.");
        }

        for (ComandaItem item : comanda.itens) {
            item.produto.quantidadeEstoqueUnidades += item.unidadesDeduzidas;
        }

        comanda.status = StatusComanda.EXCLUIDA;
        comanda.dataExclusao = Instant.now();
        comanda.observacaoExclusao = request.observacao().trim();
    }

    private Comanda findCurrentAdegaComanda(UUID uuid) {
        return comandaRepository.findByUuidAndAdega(uuid, securityService.currentAdegaUuid())
                .orElseThrow(() -> new BusinessException("Comanda não encontrada."));
    }

    private ComandaItem findCurrentAdegaComandaItem(UUID comandaUuid, UUID itemUuid) {
        return comandaItemRepository
                .findByUuidAndComandaAndAdega(itemUuid, comandaUuid, securityService.currentAdegaUuid())
                .orElseThrow(() -> new BusinessException("Item da comanda não encontrado."));
    }

    private void ensureOpen(Comanda comanda, String message) {
        if (comanda.status != StatusComanda.ABERTA) {
            throw new BusinessException(message);
        }
    }

    private void ensureTotalCoversPaid(Comanda comanda, BigDecimal total) {
        if (total.compareTo(paidValue(comanda)) < 0) {
            throw new BusinessException("O total da comanda não pode ficar menor que o valor já pago.");
        }
    }

    private BigDecimal total(Comanda comanda) {
        return comanda.itens.stream()
                .map(this::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal subtotal(ComandaItem item) {
        return item.valorCobradoUnitario.multiply(BigDecimal.valueOf(item.quantidadePedida));
    }

    private BigDecimal paidValue(Comanda comanda) {
        return comanda.valorPagoParcial == null ? BigDecimal.ZERO : comanda.valorPagoParcial;
    }

    private ItemPricing pricingFor(Produto produto, int quantidade, TipoMedidaVenda tipoMedida) {
        int unidadesParaDeduzir = quantidade;
        BigDecimal valorAplicado = produto.valorUnidade;

        if (tipoMedida == TipoMedidaVenda.CAIXA) {
            if (produto.valorCaixa == null) {
                throw new BusinessException("Produto não configurado para venda por caixa.");
            }
            unidadesParaDeduzir = quantidade * produto.unidadesPorCaixa;
            valorAplicado = produto.valorCaixa;
        }

        return new ItemPricing(unidadesParaDeduzir, valorAplicado);
    }

    private void deductStock(Produto produto, int unidadesParaDeduzir) {
        if (produto.quantidadeEstoqueUnidades < unidadesParaDeduzir) {
            throw new BusinessException("Estoque insuficiente para o produto: " + produto.nome);
        }

        produto.quantidadeEstoqueUnidades -= unidadesParaDeduzir;
    }

    private record ItemPricing(int unidadesParaDeduzir, BigDecimal valorAplicado) {
    }
}
