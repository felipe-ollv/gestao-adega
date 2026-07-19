package com.adega.repository;

import com.adega.model.Adega;
import com.adega.model.AdegaPagamento;
import com.adega.model.StatusPagamento;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import java.util.Optional;

@ApplicationScoped
public class AdegaPagamentoRepository implements PanacheRepositoryBase<AdegaPagamento, Long> {
    public Optional<AdegaPagamento> findByAdega(Adega adega) {
        return find("adega", adega).firstResultOptional();
    }

    @Transactional
    public AdegaPagamento createPending(Adega adega) {
        AdegaPagamento pagamento = new AdegaPagamento();
        pagamento.adega = adega;
        pagamento.status = StatusPagamento.PENDENTE;
        persist(pagamento);
        return pagamento;
    }

    public StatusPagamento statusByAdega(Adega adega) {
        return findByAdega(adega)
                .map((pagamento) -> pagamento.status)
                .orElse(StatusPagamento.PENDENTE);
    }
}
