package com.adega.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "comanda_item")
public class ComandaItem extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "comanda_id", nullable = false)
    public Comanda comanda;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id", nullable = false)
    public Produto produto;

    @Column(name = "quantidade_pedida", nullable = false)
    public int quantidadePedida;

    @Column(name = "unidades_deduzidas", nullable = false)
    public int unidadesDeduzidas;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_medida_vendida", nullable = false, length = 20)
    public TipoMedidaVenda tipoMedidaVendida;

    @Column(name = "valor_cobrado_unitario", nullable = false, precision = 10, scale = 2)
    public BigDecimal valorCobradoUnitario;
}
