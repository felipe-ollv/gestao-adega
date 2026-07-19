package com.adega.service;

import com.adega.model.StatusPagamento;
import com.adega.model.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TokenService {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "adega.jwt.duration.minutes")
    long durationMinutes;

    public String generate(
            Usuario usuario,
            StatusPagamento statusPagamento,
            LocalDate competenciaMensalidade,
            LocalDate vencimentoMensalidade
    ) {
        Instant now = Instant.now();
        Instant sessionExpiresAt = now.plus(Duration.ofMinutes(durationMinutes));
        Instant billingCycleExpiresAt = vencimentoMensalidade
                .plusDays(1)
                .atStartOfDay(BUSINESS_ZONE)
                .toInstant();
        Instant expiresAt = sessionExpiresAt.isBefore(billingCycleExpiresAt)
                ? sessionExpiresAt
                : billingCycleExpiresAt;

        return Jwt.issuer(issuer)
                .subject(usuario.uuid.toString())
                .upn(usuario.email)
                .groups(usuario.perfil.name())
                .claim("adegaUuid", usuario.adega.uuid.toString())
                .claim("adegaNome", usuario.adega.nome)
                .claim("perfil", usuario.perfil.name())
                .claim("nome", usuario.nome)
                .claim("statusPagamento", statusPagamento.name())
                .claim("mensalidadePaga", statusPagamento == StatusPagamento.PAGO)
                .claim("competenciaMensalidade", competenciaMensalidade.toString())
                .claim("vencimentoMensalidade", vencimentoMensalidade.toString())
                .expiresAt(expiresAt)
                .sign();
    }
}
