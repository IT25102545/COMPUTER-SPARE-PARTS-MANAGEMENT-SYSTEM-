package com.lankatech.spareparts.customer.repository;
import com.lankatech.spareparts.customer.entity.Reservation; import org.springframework.data.jpa.repository.JpaRepository;
public interface ReservationRepository extends JpaRepository<Reservation,Long>{}
