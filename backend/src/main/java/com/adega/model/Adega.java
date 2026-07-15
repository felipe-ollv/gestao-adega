package com.adega.model;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "adega")
public class Adega extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "uuid", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    public UUID uuid;

    @Column(nullable = false, length = 100)
    public String nome;

    @Column(name = "cnpj_cpf", nullable = false, unique = true, length = 14)
    public String cnpjCpf;

    @Column(name = "data_cadastro", nullable = false)
    public Instant dataCadastro;

    @PrePersist
    void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (dataCadastro == null) {
            dataCadastro = Instant.now();
        }
    }
}
