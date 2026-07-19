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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(
        name = "adega_mensalidade",
        uniqueConstraints = @UniqueConstraint(name = "uk_mensalidade_adega_competencia", columnNames = {"adega_uuid", "competencia"})
)
public class AdegaMensalidade extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adega_uuid", referencedColumnName = "uuid", nullable = false)
    public Adega adega;

    @Column(nullable = false)
    public LocalDate competencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "data_vencimento", nullable = false)
    public LocalDate dataVencimento;

    @Column(name = "data_pagamento")
    public Instant dataPagamento;

    @Column(name = "data_cadastro", nullable = false)
    public Instant dataCadastro;

    @Column(name = "data_atualizacao", nullable = false)
    public Instant dataAtualizacao;

    @PrePersist
    void prePersist() {
        Instant now = Instant.now();
        if (dataCadastro == null) {
            dataCadastro = now;
        }
        if (dataAtualizacao == null) {
            dataAtualizacao = now;
        }
    }

    @PreUpdate
    void preUpdate() {
        dataAtualizacao = Instant.now();
    }
}
