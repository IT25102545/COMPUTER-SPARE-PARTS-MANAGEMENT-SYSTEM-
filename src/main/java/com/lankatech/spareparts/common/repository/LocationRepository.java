package com.lankatech.spareparts.common.repository;

import com.lankatech.spareparts.common.entity.Location;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long> {
}