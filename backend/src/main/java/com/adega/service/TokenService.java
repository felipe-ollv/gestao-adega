package com.adega.service;

import com.adega.model.StatusPagamento;
import com.adega.model.Usuario;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TokenService {
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
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(durationMinutes));

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
