package com.lankatech.spareparts.supplier.repository;

import com.lankatech.spareparts.supplier.entity.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {}
