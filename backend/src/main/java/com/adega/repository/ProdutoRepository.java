package com.adega.repository;

import com.adega.model.Produto;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class ProdutoRepository implements PanacheRepositoryBase<Produto, Long> {
    public Optional<Produto> findByUuidAndAdega(UUID uuid, UUID adegaUuid) {
        return find("uuid = ?1 and adega.uuid = ?2", uuid, adegaUuid).firstResultOptional();
    }

    public List<Produto> listByAdega(UUID adegaUuid) {
        return list("adega.uuid = ?1 and ativo = true order by nome", adegaUuid);
    }
}
