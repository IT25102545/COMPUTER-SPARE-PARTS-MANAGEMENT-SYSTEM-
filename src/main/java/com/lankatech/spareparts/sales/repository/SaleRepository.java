package com.lankatech.spareparts.sales.repository;

import com.lankatech.spareparts.sales.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SaleRepository extends JpaRepository<Sale, Long> {
}