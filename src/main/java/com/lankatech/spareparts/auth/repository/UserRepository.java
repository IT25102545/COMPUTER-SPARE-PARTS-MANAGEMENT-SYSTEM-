package com.lankatech.spareparts.auth.repository;

import com.lankatech.spareparts.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}