package com.adega.repository;

import com.adega.model.ComandaItem;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ComandaItemRepository implements PanacheRepositoryBase<ComandaItem, Long> {
    public Optional<ComandaItem> findByUuidAndComandaAndAdega(UUID uuid, UUID comandaUuid, UUID adegaUuid) {
        return find("uuid = ?1 and comanda.uuid = ?2 and comanda.adega.uuid = ?3", uuid, comandaUuid, adegaUuid)
                .firstResultOptional();
    }
}
