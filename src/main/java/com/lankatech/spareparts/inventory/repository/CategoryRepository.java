package com.lankatech.spareparts.inventory.repository;

import com.lankatech.spareparts.inventory.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}