package com.adega.service.billing;

import com.adega.model.AdegaMensalidade;
import com.adega.model.StatusPagamento;
import java.time.LocalDateTime;
import java.util.Objects;

public final class PaidBillingCycleNormalizer {
    private PaidBillingCycleNormalizer() {
    }

    public static BillingCycle expectedCycle(AdegaMensalidade monthlyPayment) {
        Objects.requireNonNull(monthlyPayment, "A mensalidade é obrigatória.");

        if (monthlyPayment.status != StatusPagamento.PAGO) {
            throw new IllegalArgumentException("Apenas mensalidades pagas podem ter a vigência normalizada.");
        }

        LocalDateTime paymentDate = monthlyPayment.dataPagamento != null
                ? monthlyPayment.dataPagamento
                : Objects.requireNonNull(
                        monthlyPayment.dataAtualizacao,
                        "A mensalidade paga precisa ter uma data de pagamento."
                );
        return BillingCycleCalculator.fromPaymentDate(paymentDate.toLocalDate());
    }

    public static BillingCycle normalize(AdegaMensalidade monthlyPayment) {
        BillingCycle cycle = expectedCycle(monthlyPayment);
        LocalDateTime paymentDate = monthlyPayment.dataPagamento != null
                ? monthlyPayment.dataPagamento
                : monthlyPayment.dataAtualizacao;

        monthlyPayment.dataPagamento = paymentDate;
        monthlyPayment.competencia = cycle.startDate();
        monthlyPayment.dataVencimento = cycle.endDate();

        return cycle;
    }
}
