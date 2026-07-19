package com.adega.service.email;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.adega.model.Adega;
import com.adega.model.TipoNotificacaoEmail;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EmailNotificationSchedulerTest {
    @Test
    void notificationReferenceIncludesAdegaUuid() {
        Adega firstAdega = new Adega();
        firstAdega.uuid = UUID.fromString("11111111-1111-1111-1111-111111111111");
        Adega secondAdega = new Adega();
        secondAdega.uuid = UUID.fromString("22222222-2222-2222-2222-222222222222");

        String firstReference = EmailNotificationScheduler.notificationReference(
                firstAdega,
                TipoNotificacaoEmail.PAGAMENTO_CONFIRMADO,
                "2026-07-19",
                "gestor@example.com"
        );
        String secondReference = EmailNotificationScheduler.notificationReference(
                secondAdega,
                TipoNotificacaoEmail.PAGAMENTO_CONFIRMADO,
                "2026-07-19",
                "gestor@example.com"
        );

        assertNotEquals(firstReference, secondReference);
        assertTrue(firstReference.startsWith(firstAdega.uuid.toString()));
        assertTrue(secondReference.startsWith(secondAdega.uuid.toString()));
    }

    @Test
    void legacyNotificationReferenceKeepsPreviousFormatAvailableForLookup() {
        String legacyReference = EmailNotificationScheduler.legacyNotificationReference(
                TipoNotificacaoEmail.PAGAMENTO_CONFIRMADO,
                "2026-07-19",
                "gestor@example.com"
        );

        assertEquals("PAGAMENTO_CONFIRMADO:2026-07-19:gestor@example.com", legacyReference);
    }
}
