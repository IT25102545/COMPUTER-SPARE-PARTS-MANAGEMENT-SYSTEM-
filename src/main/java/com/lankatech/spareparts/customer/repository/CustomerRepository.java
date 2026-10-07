package com.lankatech.spareparts.customer.repository;

import com.lankatech.spareparts.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
}