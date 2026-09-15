package com.lankatech.spareparts.sales.repository;

import com.lankatech.spareparts.sales.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}