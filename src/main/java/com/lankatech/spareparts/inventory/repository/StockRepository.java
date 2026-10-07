package com.lankatech.spareparts.inventory.repository;

import com.lankatech.spareparts.inventory.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;import java.util.List;

public interface StockRepository extends JpaRepository<Stock, Long> {

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select s from Stock s where s.sparePart.sparePartId = :partId and s.location.locationId = :locationId")
    Optional<Stock> findForUpdate(
            @org.springframework.data.repository.query.Param("partId") Long partId,
            @org.springframework.data.repository.query.Param("locationId") Long locationId);

    Optional<Stock> findBySparePartSparePartIdAndLocationLocationId(
            Long sparePartId,
            Long locationId
    );

    List<Stock> findBySparePartSparePartId(Long sparePartId);
}