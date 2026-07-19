package com.adega.repository;

import com.adega.model.Adega;
import com.adega.model.AdegaMensalidade;
import com.adega.model.StatusPagamento;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Optional;

@ApplicationScoped
public class AdegaMensalidadeRepository implements PanacheRepositoryBase<AdegaMensalidade, Long> {
    public Optional<AdegaMensalidade> findByAdegaAndCompetencia(Adega adega, LocalDate competencia) {
        return find("adega = ?1 and competencia = ?2", adega, competencia).firstResultOptional();
    }

    @Transactional
    public AdegaMensalidade createPendingCurrentMonth(Adega adega) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate competencia = currentMonth.atDay(1);

        return findByAdegaAndCompetencia(adega, competencia)
                .orElseGet(() -> {
                    AdegaMensalidade mensalidade = new AdegaMensalidade();
                    mensalidade.adega = adega;
                    mensalidade.competencia = competencia;
                    mensalidade.status = StatusPagamento.PENDENTE;
                    mensalidade.dataVencimento = currentMonth.atEndOfMonth();
                    persist(mensalidade);
                    return mensalidade;
                });
    }

    @Transactional
    public AdegaMensalidade currentMonthStatus(Adega adega) {
        YearMonth currentMonth = YearMonth.now();
        LocalDate competencia = currentMonth.atDay(1);

        return findByAdegaAndCompetencia(adega, competencia)
                .orElseGet(() -> {
                    AdegaMensalidade mensalidade = new AdegaMensalidade();
                    mensalidade.adega = adega;
                    mensalidade.competencia = competencia;
                    mensalidade.status = StatusPagamento.PENDENTE;
                    mensalidade.dataVencimento = currentMonth.atEndOfMonth();
                    persist(mensalidade);
                    return mensalidade;
                });
    }
}
