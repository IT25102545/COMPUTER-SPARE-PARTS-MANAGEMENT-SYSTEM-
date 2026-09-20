package com.lankatech.spareparts.inventory.repository;

import com.lankatech.spareparts.inventory.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BrandRepository extends JpaRepository<Brand, Long> {
}
