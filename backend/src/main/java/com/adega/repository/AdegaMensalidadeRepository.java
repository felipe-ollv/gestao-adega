package com.adega.repository;

import com.adega.model.Adega;
import com.adega.model.AdegaMensalidade;
import com.adega.model.StatusPagamento;
import com.adega.service.billing.BillingCycle;
import com.adega.service.billing.PaidBillingCycleNormalizer;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationScoped
public class AdegaMensalidadeRepository implements PanacheRepositoryBase<AdegaMensalidade, Long> {
    public Optional<AdegaMensalidade> findByAdegaAndCompetencia(Adega adega, LocalDate competencia) {
        return find("adega = ?1 and competencia = ?2", adega, competencia).firstResultOptional();
    }

    @Transactional
    public AdegaMensalidade createPendingRegistrationCycle(Adega adega) {
        return findOrCreatePending(adega, registrationDate(adega));
    }

    @Transactional
    public AdegaMensalidade createPendingCurrentCycle(Adega adega) {
        LocalDate today = com.adega.util.BusinessTime.today();
        normalizePaidCycles(adega);

        return findActivePaid(adega, today)
                .or(() -> findLatestPending(adega))
                .orElseGet(() -> findOrCreatePending(adega, today));
    }

    @Transactional
    public void normalizePaidCycles(Adega adega) {
        List<AdegaMensalidade> paidCycles = list(
                "adega = ?1 and status = ?2 order by dataPagamento, id",
                adega,
                StatusPagamento.PAGO
        );

        paidCycles.forEach(this::normalizePaidCycle);
    }

    public Optional<AdegaMensalidade> findRegistrationCycle(Adega adega) {
        return findByAdegaAndCompetencia(adega, registrationDate(adega));
    }

    public Optional<AdegaMensalidade> findActivePaid(Adega adega, LocalDate referenceDate) {
        return find(
                "adega = ?1 and status = ?2 and competencia <= ?3 "
                        + "and dataVencimento >= ?3 order by dataVencimento desc, id desc",
                adega,
                StatusPagamento.PAGO,
                referenceDate
        ).firstResultOptional();
    }

    private Optional<AdegaMensalidade> findLatestPending(Adega adega) {
        return find(
                "adega = ?1 and status = ?2 order by id desc",
                adega,
                StatusPagamento.PENDENTE
        ).firstResultOptional();
    }

    private AdegaMensalidade findOrCreatePending(Adega adega, LocalDate requestDate) {
        return findByAdegaAndCompetencia(adega, requestDate)
                .orElseGet(() -> {
                    AdegaMensalidade mensalidade = new AdegaMensalidade();
                    mensalidade.adega = adega;
                    mensalidade.competencia = requestDate;
                    mensalidade.status = StatusPagamento.PENDENTE;
                    mensalidade.dataVencimento = requestDate;
                    persist(mensalidade);
                    return mensalidade;
                });
    }

    private void normalizePaidCycle(AdegaMensalidade monthlyPayment) {
        BillingCycle expectedCycle = PaidBillingCycleNormalizer.expectedCycle(monthlyPayment);

        findByAdegaAndCompetencia(monthlyPayment.adega, expectedCycle.startDate())
                .filter(conflict -> !Objects.equals(conflict.id, monthlyPayment.id))
                .ifPresent(conflict -> removeConflictingCycle(monthlyPayment, conflict));

        PaidBillingCycleNormalizer.normalize(monthlyPayment);
    }

    private void removeConflictingCycle(
            AdegaMensalidade monthlyPayment,
            AdegaMensalidade conflictingCycle
    ) {
        if (conflictingCycle.status == StatusPagamento.PAGO) {
            throw new IllegalStateException(
                    "Existem duas mensalidades pagas para a adega "
                            + monthlyPayment.adega.uuid
                            + " com início em "
                            + conflictingCycle.competencia
                            + "."
            );
        }

        delete(conflictingCycle);
        flush();
    }

    private LocalDate registrationDate(Adega adega) {
        return adega.dataCadastro.toLocalDate();
    }

    public List<AdegaMensalidade> listAllOrdered() {
        return list("order by competencia, adega.id");
    }
}
