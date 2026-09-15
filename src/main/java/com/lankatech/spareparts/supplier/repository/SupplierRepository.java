package com.lankatech.spareparts.supplier.repository;

import com.lankatech.spareparts.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupplierRepository extends JpaRepository<Supplier, Long> {}
