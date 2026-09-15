package com.lankatech.spareparts.sales.repository;

import com.lankatech.spareparts.sales.entity.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
}