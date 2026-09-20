package com.lankatech.spareparts.transfer.repository;

import com.lankatech.spareparts.transfer.entity.StockTransfer;
import com.lankatech.spareparts.transfer.enums.TransferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockTransferRepository
        extends JpaRepository<StockTransfer, Long> {

    List<StockTransfer> findByStatus(TransferStatus status);

    List<StockTransfer> findBySourceLocationLocationId(Long locationId);

    List<StockTransfer> findByDestinationLocationLocationId(Long locationId);

    List<StockTransfer> findAllByOrderByRequestDateDesc();
}