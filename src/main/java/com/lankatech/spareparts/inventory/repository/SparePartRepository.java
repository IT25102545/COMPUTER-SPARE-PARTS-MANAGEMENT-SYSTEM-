package com.lankatech.spareparts.inventory.repository;

import com.lankatech.spareparts.inventory.entity.SparePart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SparePartRepository extends JpaRepository<SparePart, Long> {

    Optional<SparePart> findByPartCode(String partCode);
}
