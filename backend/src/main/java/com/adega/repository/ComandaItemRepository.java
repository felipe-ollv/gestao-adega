package com.adega.repository;

import com.adega.model.ComandaItem;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ComandaItemRepository implements PanacheRepositoryBase<ComandaItem, Long> {
}
