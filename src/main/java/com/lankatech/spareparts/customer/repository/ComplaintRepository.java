package com.lankatech.spareparts.customer.repository;
import com.lankatech.spareparts.customer.entity.Complaint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.Optional;
public interface ComplaintRepository extends JpaRepository<Complaint,Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Complaint c where c.complaintId = :id")
    Optional<Complaint> findForUpdate(@Param("id") Long id);
}
