package com.adega.repository;

import com.adega.model.Adega;
import com.adega.model.AdegaMensalidade;
import com.adega.model.StatusPagamento;
import com.adega.service.billing.BillingCycle;
import com.adega.service.billing.BillingCycleCalculator;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import java.util.List;

@ApplicationScoped
public class AdegaMensalidadeRepository implements PanacheRepositoryBase<AdegaMensalidade, Long> {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");

    public Optional<AdegaMensalidade> findByAdegaAndCompetencia(Adega adega, LocalDate competencia) {
        return find("adega = ?1 and competencia = ?2", adega, competencia).firstResultOptional();
    }

    @Transactional
    public AdegaMensalidade createPendingRegistrationCycle(Adega adega) {
        BillingCycle cycle = BillingCycleCalculator.firstCycle(registrationDate(adega));
        return findOrCreatePending(adega, cycle);
    }

    @Transactional
    public AdegaMensalidade createPendingCurrentCycle(Adega adega) {
        BillingCycle cycle = BillingCycleCalculator.currentCycle(
                registrationDate(adega),
                LocalDate.now(BUSINESS_ZONE)
        );
        return findOrCreatePending(adega, cycle);
    }

    public Optional<AdegaMensalidade> findRegistrationCycle(Adega adega) {
        LocalDate cycleStart = BillingCycleCalculator.firstCycle(registrationDate(adega)).startDate();
        return findByAdegaAndCompetencia(adega, cycleStart);
    }

    private AdegaMensalidade findOrCreatePending(Adega adega, BillingCycle cycle) {
        return findByAdegaAndCompetencia(adega, cycle.startDate())
                .orElseGet(() -> {
                    AdegaMensalidade mensalidade = new AdegaMensalidade();
                    mensalidade.adega = adega;
                    mensalidade.competencia = cycle.startDate();
                    mensalidade.status = StatusPagamento.PENDENTE;
                    mensalidade.dataVencimento = cycle.endDate();
                    persist(mensalidade);
                    return mensalidade;
                });
    }

    private LocalDate registrationDate(Adega adega) {
        return adega.dataCadastro.atZone(BUSINESS_ZONE).toLocalDate();
    }

    public List<AdegaMensalidade> listAllOrdered() {
        return list("order by competencia, adega.id");
    }
}
