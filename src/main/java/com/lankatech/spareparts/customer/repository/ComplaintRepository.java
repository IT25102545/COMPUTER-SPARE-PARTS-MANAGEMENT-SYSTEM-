package com.lankatech.spareparts.customer.repository;
import com.lankatech.spareparts.customer.entity.Complaint; import org.springframework.data.jpa.repository.JpaRepository;
public interface ComplaintRepository extends JpaRepository<Complaint,Long>{}