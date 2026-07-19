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
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "notificacao_email")
public class NotificacaoEmail extends PanacheEntityBase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "uuid", columnDefinition = "BINARY(16)", nullable = false, unique = true)
    public UUID uuid;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "adega_uuid", referencedColumnName = "uuid", nullable = false)
    public Adega adega;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    public TipoNotificacaoEmail tipo;

    @Column(name = "chave_referencia", nullable = false, unique = true, length = 255)
    public String chaveReferencia;

    @Column(nullable = false, length = 120)
    public String destinatario;

    @Column(name = "provider_id", length = 100)
    public String providerId;

    @Column(name = "data_envio", nullable = false)
    public Instant dataEnvio;

    @PrePersist
    void prePersist() {
        if (uuid == null) {
            uuid = UUID.randomUUID();
        }
        if (dataEnvio == null) {
            dataEnvio = Instant.now();
        }
    }
}
