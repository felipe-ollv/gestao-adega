package com.adega.repository;

import com.adega.model.Adega;
import com.adega.model.NotificacaoEmail;
import com.adega.model.TipoNotificacaoEmail;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class NotificacaoEmailRepository implements PanacheRepositoryBase<NotificacaoEmail, Long> {
    public boolean existsByReference(String reference) {
        return count("chaveReferencia", reference) > 0;
    }

    public void record(
            Adega adega,
            TipoNotificacaoEmail tipo,
            String reference,
            String recipient,
            String providerId
    ) {
        NotificacaoEmail notification = new NotificacaoEmail();
        notification.adega = adega;
        notification.tipo = tipo;
        notification.chaveReferencia = reference;
        notification.destinatario = recipient;
        notification.providerId = providerId;
        persist(notification);
    }
}
