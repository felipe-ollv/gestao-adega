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
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "adega_pagamento")
public class AdegaPagamento extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adega_uuid", referencedColumnName = "uuid", nullable = false, unique = true)
    public Adega adega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    public StatusPagamento status = StatusPagamento.PENDENTE;

    @Column(name = "data_cadastro", nullable = false)
    public Instant dataCadastro;

    @Column(name = "data_atualizacao", nullable = false)
    public Instant dataAtualizacao;

    @Column(name = "data_pagamento")
    public Instant dataPagamento;

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
