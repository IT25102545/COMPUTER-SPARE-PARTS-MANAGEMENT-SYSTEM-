package com.lankatech.spareparts.customer.repository;
import com.lankatech.spareparts.customer.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface ReservationRepository extends JpaRepository<Reservation,Long> {
    boolean existsByCustomerCustomerId(Long customerId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.reservationId = :id")
    Optional<Reservation> findForUpdate(@Param("id") Long id);
}
