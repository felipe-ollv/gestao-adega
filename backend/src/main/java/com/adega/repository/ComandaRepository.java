package com.adega.repository;

import com.adega.model.Comanda;
import com.adega.model.StatusComanda;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ComandaRepository implements PanacheRepositoryBase<Comanda, Long> {
    public Optional<Comanda> findByUuidAndAdega(UUID uuid, UUID adegaUuid) {
        return find("uuid = ?1 and adega.uuid = ?2", uuid, adegaUuid).firstResultOptional();
    }

    public List<Comanda> listByAdega(UUID adegaUuid, StatusComanda status) {
        if (status == null) {
            return list("adega.uuid = ?1 and status <> ?2 order by dataAbertura desc", adegaUuid, StatusComanda.EXCLUIDA);
        }
        return list("adega.uuid = ?1 and status = ?2 order by dataAbertura desc", adegaUuid, status);
    }
}
