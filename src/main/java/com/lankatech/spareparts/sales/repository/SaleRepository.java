package com.lankatech.spareparts.sales.repository;

import com.lankatech.spareparts.sales.entity.Sale;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Override
    @EntityGraph(attributePaths = {
            "items",
            "items.sparePart",
            "items.sparePart.category",
            "items.sparePart.brand",
            "customer",
            "location",
            "cashierUser",
            "cashierUser.role"
    })
    List<Sale> findAll();
}