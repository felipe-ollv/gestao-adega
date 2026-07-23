package com.adega.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.adega.dto.AdicionarItemRequest;
import com.adega.dto.AdicionarItensRequest;
import com.adega.dto.ComandaResponse;
import com.adega.dto.AtualizarItemRequest;
import com.adega.exception.BusinessException;
import com.adega.model.Comanda;
import com.adega.model.ComandaItem;
import com.adega.model.Produto;
import com.adega.model.StatusComanda;
import com.adega.model.TipoMedidaVenda;
import com.adega.repository.AdegaRepository;
import com.adega.repository.ComandaItemRepository;
import com.adega.repository.ComandaRepository;
import com.adega.repository.ProdutoRepository;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ComandaServiceTest {
    private final UUID adegaUuid = UUID.randomUUID();
    private final UUID comandaUuid = UUID.randomUUID();

    private ComandaService service;
    private ComandaRepository comandaRepository;
    private ComandaItemRepository comandaItemRepository;
    private ProdutoRepository produtoRepository;

    @BeforeEach
    void setUp() {
        service = new ComandaService();
        comandaRepository = mock(ComandaRepository.class);
        comandaItemRepository = mock(ComandaItemRepository.class);
        produtoRepository = mock(ProdutoRepository.class);
        SecurityService securityService = mock(SecurityService.class);

        service.comandaRepository = comandaRepository;
        service.comandaItemRepository = comandaItemRepository;
        service.produtoRepository = produtoRepository;
        service.adegaRepository = mock(AdegaRepository.class);
        service.securityService = securityService;

        when(securityService.currentAdegaUuid()).thenReturn(adegaUuid);
        doAnswer(invocation -> {
            ComandaItem item = invocation.getArgument(0);
            if (item.uuid == null) {
                item.uuid = UUID.randomUUID();
            }
            return null;
        }).when(comandaItemRepository).persist(any(ComandaItem.class));
    }

    @Test
    void addsComboWithIndividualStockAndSummedPrice() {
        Comanda comanda = openComanda();
        Produto whisky = produto("Whisky", 10, 1, "10.00", null);
        Produto energetico = produto("Energético", 24, 6, "7.00", "36.00");
        stubComanda(comanda);
        stubProduto(whisky);
        stubProduto(energetico);

        ComandaResponse response = service.addItems(
                comandaUuid,
                new AdicionarItensRequest(List.of(
                        new AdicionarItemRequest(whisky.uuid, 2, TipoMedidaVenda.UNIDADE),
                        new AdicionarItemRequest(energetico.uuid, 1, TipoMedidaVenda.CAIXA)
                ))
        );

        assertEquals(8, whisky.quantidadeEstoqueUnidades);
        assertEquals(18, energetico.quantidadeEstoqueUnidades);
        assertEquals(new BigDecimal("56.00"), response.total());
        assertEquals(2, response.itens().size());
        assertNotNull(response.itens().get(0).grupoUuid());
        assertEquals(response.itens().get(0).grupoUuid(), response.itens().get(1).grupoUuid());
        assertEquals(0, response.itens().get(0).ordemGrupo());
        assertEquals(1, response.itens().get(1).ordemGrupo());
    }

    @Test
    void doesNotChangeAnyStockWhenAComboComponentIsUnavailable() {
        Comanda comanda = openComanda();
        Produto whisky = produto("Whisky", 10, 1, "10.00", null);
        Produto gelo = produto("Gelo", 1, 1, "5.00", null);
        stubComanda(comanda);
        stubProduto(whisky);
        stubProduto(gelo);

        assertThrows(
                BusinessException.class,
                () -> service.addItems(
                        comandaUuid,
                        new AdicionarItensRequest(List.of(
                                new AdicionarItemRequest(whisky.uuid, 2, TipoMedidaVenda.UNIDADE),
                                new AdicionarItemRequest(gelo.uuid, 2, TipoMedidaVenda.UNIDADE)
                        ))
                )
        );

        assertEquals(10, whisky.quantidadeEstoqueUnidades);
        assertEquals(1, gelo.quantidadeEstoqueUnidades);
        verify(comandaItemRepository, never()).persist(any(ComandaItem.class));
    }

    @Test
    void rejectsRepeatedProductsInTheSameCombo() {
        Comanda comanda = openComanda();
        Produto whisky = produto("Whisky", 10, 1, "10.00", null);
        stubComanda(comanda);

        assertThrows(
                BusinessException.class,
                () -> service.addItems(
                        comandaUuid,
                        new AdicionarItensRequest(List.of(
                                new AdicionarItemRequest(whisky.uuid, 1, TipoMedidaVenda.UNIDADE),
                                new AdicionarItemRequest(whisky.uuid, 2, TipoMedidaVenda.UNIDADE)
                        ))
                )
        );

        verify(produtoRepository, never()).findByUuidAndAdega(any(), any());
    }

    @Test
    void rejectsChangingAComponentToAnotherProductAlreadyInTheGroup() {
        Comanda comanda = openComanda();
        Produto whisky = produto("Whisky", 10, 1, "10.00", null);
        Produto gelo = produto("Gelo", 10, 1, "5.00", null);
        UUID grupoUuid = UUID.randomUUID();
        ComandaItem whiskyItem = item(comanda, whisky, grupoUuid, 0);
        ComandaItem geloItem = item(comanda, gelo, grupoUuid, 1);
        comanda.itens.addAll(List.of(whiskyItem, geloItem));
        stubComanda(comanda);
        stubProduto(gelo);
        when(comandaItemRepository.findByUuidAndComandaAndAdega(
                whiskyItem.uuid,
                comandaUuid,
                adegaUuid
        )).thenReturn(Optional.of(whiskyItem));

        assertThrows(
                BusinessException.class,
                () -> service.updateItem(
                        comandaUuid,
                        whiskyItem.uuid,
                        new AtualizarItemRequest(gelo.uuid, 1, TipoMedidaVenda.UNIDADE)
                )
        );

        assertEquals(10, whisky.quantidadeEstoqueUnidades);
        assertEquals(10, gelo.quantidadeEstoqueUnidades);
    }

    @Test
    void deletingAComponentReturnsOnlyItsStock() {
        Comanda comanda = openComanda();
        Produto whisky = produto("Whisky", 8, 1, "10.00", null);
        Produto gelo = produto("Gelo", 9, 1, "5.00", null);
        UUID grupoUuid = UUID.randomUUID();
        ComandaItem whiskyItem = item(comanda, whisky, grupoUuid, 0);
        whiskyItem.quantidadePedida = 2;
        whiskyItem.unidadesDeduzidas = 2;
        ComandaItem geloItem = item(comanda, gelo, grupoUuid, 1);
        comanda.itens.addAll(List.of(whiskyItem, geloItem));
        stubComanda(comanda);
        when(comandaItemRepository.findByUuidAndComandaAndAdega(
                geloItem.uuid,
                comandaUuid,
                adegaUuid
        )).thenReturn(Optional.of(geloItem));

        ComandaResponse response = service.deleteItem(comandaUuid, geloItem.uuid);

        assertEquals(8, whisky.quantidadeEstoqueUnidades);
        assertEquals(10, gelo.quantidadeEstoqueUnidades);
        assertEquals(1, response.itens().size());
        assertEquals(whisky.uuid, response.itens().get(0).produtoUuid());
        verify(comandaItemRepository).delete(geloItem);
    }

    private Comanda openComanda() {
        Comanda comanda = new Comanda();
        comanda.uuid = comandaUuid;
        comanda.status = StatusComanda.ABERTA;
        comanda.nomeResponsavel = "Cliente";
        comanda.itens = new ArrayList<>();
        comanda.valorPagoParcial = BigDecimal.ZERO;
        return comanda;
    }

    private Produto produto(
            String nome,
            int estoque,
            int unidadesPorCaixa,
            String valorUnidade,
            String valorCaixa
    ) {
        Produto produto = new Produto();
        produto.uuid = UUID.randomUUID();
        produto.nome = nome;
        produto.ativo = true;
        produto.quantidadeEstoqueUnidades = estoque;
        produto.unidadesPorCaixa = unidadesPorCaixa;
        produto.valorUnidade = new BigDecimal(valorUnidade);
        produto.valorCaixa = valorCaixa == null ? null : new BigDecimal(valorCaixa);
        return produto;
    }

    private ComandaItem item(Comanda comanda, Produto produto, UUID grupoUuid, int ordemGrupo) {
        ComandaItem item = new ComandaItem();
        item.uuid = UUID.randomUUID();
        item.comanda = comanda;
        item.produto = produto;
        item.quantidadePedida = 1;
        item.unidadesDeduzidas = 1;
        item.tipoMedidaVendida = TipoMedidaVenda.UNIDADE;
        item.valorCobradoUnitario = produto.valorUnidade;
        item.grupoUuid = grupoUuid;
        item.ordemGrupo = ordemGrupo;
        return item;
    }

    private void stubComanda(Comanda comanda) {
        when(comandaRepository.findByUuidAndAdega(comandaUuid, adegaUuid))
                .thenReturn(Optional.of(comanda));
    }

    private void stubProduto(Produto produto) {
        when(produtoRepository.findByUuidAndAdega(produto.uuid, adegaUuid))
                .thenReturn(Optional.of(produto));
    }
}
