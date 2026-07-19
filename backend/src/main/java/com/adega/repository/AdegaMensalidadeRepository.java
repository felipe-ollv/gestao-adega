package com.adega.repository;

import com.adega.model.Adega;
import com.adega.model.AdegaMensalidade;
import com.adega.model.StatusPagamento;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class AdegaMensalidadeRepository implements PanacheRepositoryBase<AdegaMensalidade, Long> {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Sao_Paulo");

    public Optional<AdegaMensalidade> findByAdegaAndCompetencia(Adega adega, LocalDate competencia) {
        return find("adega = ?1 and competencia = ?2", adega, competencia).firstResultOptional();
    }

    @Transactional
    public AdegaMensalidade createPendingRegistrationCycle(Adega adega) {
        return findOrCreatePending(adega, registrationDate(adega));
    }

    @Transactional
    public AdegaMensalidade createPendingCurrentCycle(Adega adega) {
        LocalDate today = LocalDate.now(BUSINESS_ZONE);

        return findActivePaid(adega, today)
                .or(() -> findLatestPending(adega))
                .orElseGet(() -> findOrCreatePending(adega, today));
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

    private LocalDate registrationDate(Adega adega) {
        return adega.dataCadastro.atZone(BUSINESS_ZONE).toLocalDate();
    }

    public List<AdegaMensalidade> listAllOrdered() {
        return list("order by competencia, adega.id");
    }
}
