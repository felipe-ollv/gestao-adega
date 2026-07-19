package com.adega.service.email;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ResendEmailServiceTest {
    @Test
    void idempotencyKeyChangesWhenPayloadChanges() {
        String seed = "adega:PAGAMENTO_CONFIRMADO:2026-07-19:gestor@example.com";

        String firstKey = ResendEmailService.buildIdempotencyKey(seed, "{\"subject\":\"Pagamento confirmado\"}");
        String secondKey = ResendEmailService.buildIdempotencyKey(seed, "{\"subject\":\"Pagamento confirmado!\"}");

        assertNotEquals(firstKey, secondKey);
        assertTrue(firstKey.startsWith("gestao-"));
    }

    @Test
    void idempotencyKeyIsStableForSameSeedAndPayload() {
        String seed = "adega:PAGAMENTO_CONFIRMADO:2026-07-19:gestor@example.com";
        String payload = "{\"subject\":\"Pagamento confirmado\"}";

        assertEquals(
                ResendEmailService.buildIdempotencyKey(seed, payload),
                ResendEmailService.buildIdempotencyKey(seed, payload)
        );
    }
}
