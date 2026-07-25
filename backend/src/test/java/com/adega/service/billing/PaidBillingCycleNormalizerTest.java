package com.adega.service.billing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.adega.model.AdegaMensalidade;
import com.adega.model.StatusPagamento;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;

class PaidBillingCycleNormalizerTest {
    @Test
    void replacesCalendarMonthWithPeriodStartingOnPaymentDate() {
        AdegaMensalidade monthlyPayment = new AdegaMensalidade();
        monthlyPayment.status = StatusPagamento.PAGO;
        monthlyPayment.competencia = LocalDate.of(2026, 7, 1);
        monthlyPayment.dataVencimento = LocalDate.of(2026, 7, 31);
        monthlyPayment.dataPagamento = LocalDateTime.of(2026, 7, 25, 14, 30);

        PaidBillingCycleNormalizer.normalize(monthlyPayment);

        assertEquals(LocalDate.of(2026, 7, 25), monthlyPayment.competencia);
        assertEquals(LocalDate.of(2026, 8, 24), monthlyPayment.dataVencimento);
        assertEquals(
                LocalDateTime.of(2026, 7, 25, 14, 30),
                monthlyPayment.dataPagamento
        );
    }

    @Test
    void freezesUpdateDateAsPaymentDateWhenManualUpdateDidNotFillIt() {
        AdegaMensalidade monthlyPayment = new AdegaMensalidade();
        monthlyPayment.status = StatusPagamento.PAGO;
        monthlyPayment.competencia = LocalDate.of(2026, 7, 1);
        monthlyPayment.dataVencimento = LocalDate.of(2026, 7, 31);
        monthlyPayment.dataAtualizacao = LocalDateTime.of(2026, 7, 25, 16, 45);

        PaidBillingCycleNormalizer.normalize(monthlyPayment);

        assertEquals(
                LocalDateTime.of(2026, 7, 25, 16, 45),
                monthlyPayment.dataPagamento
        );
        assertEquals(LocalDate.of(2026, 7, 25), monthlyPayment.competencia);
        assertEquals(LocalDate.of(2026, 8, 24), monthlyPayment.dataVencimento);
    }

    @Test
    void rejectsPendingMonthlyPayment() {
        AdegaMensalidade monthlyPayment = new AdegaMensalidade();
        monthlyPayment.status = StatusPagamento.PENDENTE;

        assertThrows(
                IllegalArgumentException.class,
                () -> PaidBillingCycleNormalizer.normalize(monthlyPayment)
        );
    }
}
