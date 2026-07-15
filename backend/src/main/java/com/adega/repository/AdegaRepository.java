package com.adega.repository;

import com.adega.model.Adega;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class AdegaRepository implements PanacheRepositoryBase<Adega, Long> {
    public Optional<Adega> findByUuid(UUID uuid) {
        return find("uuid", uuid).firstResultOptional();
    }

    public boolean existsByCnpjCpf(String cnpjCpf) {
        return count("cnpjCpf", cnpjCpf) > 0;
    }
}
