package com.lankatech.spareparts.inventory.repository;

import com.lankatech.spareparts.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySparePartSparePartIdAndLocationLocationId(Long sparePartId, Long locationId);

    List<Stock> findBySparePartSparePartId(Long sparePartId);
}