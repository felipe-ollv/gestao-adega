package com.adega.service;

import com.adega.exception.ForbiddenOperationException;
import com.adega.model.PerfilUsuario;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.UUID;
import org.eclipse.microprofile.jwt.JsonWebToken;

@ApplicationScoped
public class SecurityService {
    @Inject
    JsonWebToken jwt;

    public UUID currentAdegaUuid() {
        String value = jwt.getClaim("adegaUuid");
        if (value == null || value.isBlank()) {
            throw new ForbiddenOperationException("Token sem adega associada.");
        }
        return UUID.fromString(value);
    }

    public boolean isGestor() {
        return jwt.getGroups().contains(PerfilUsuario.GESTOR.name());
    }
}
