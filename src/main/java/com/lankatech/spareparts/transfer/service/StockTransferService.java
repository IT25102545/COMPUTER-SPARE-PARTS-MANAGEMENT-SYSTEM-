package com.lankatech.spareparts.transfer.service;

import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.auth.repository.UserRepository;
import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.common.repository.LocationRepository;
import com.lankatech.spareparts.transfer.dto.StockTransferRequestDTO;
import com.lankatech.spareparts.transfer.entity.StockTransfer;
import com.lankatech.spareparts.transfer.enums.TransferStatus;
import com.lankatech.spareparts.transfer.repository.StockTransferRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    public StockTransferService(
            StockTransferRepository stockTransferRepository,
            LocationRepository locationRepository,
            UserRepository userRepository) {

        this.stockTransferRepository = stockTransferRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
    }

    // Get all transfers - newest first
    public List<StockTransfer> getAllTransfers() {

        return stockTransferRepository
                .findAllByOrderByRequestDateDesc();
    }

    // Get one transfer using ID
    public StockTransfer getTransferById(Long transferId) {

        return stockTransferRepository
                .findById(transferId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Stock transfer not found: " + transferId
                        )
                );
    }

    // Create new transfer using DTO
    public StockTransfer createTransfer(
            StockTransferRequestDTO request) {

        // Find source location
        Location sourceLocation =
                locationRepository
                        .findById(request.getSourceLocationId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Source location not found"
                                )
                        );

        // Find destination location
        Location destinationLocation =
                locationRepository
                        .findById(request.getDestinationLocationId())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Destination location not found"
                                )
                        );

        // Find user who created request
        User requestedBy =
                userRepository
                        .findById(request.getRequestedById())
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Requested user not found"
                                )
                        );

        // Source and destination cannot be same
        if (sourceLocation.getLocationId()
                .equals(destinationLocation.getLocationId())) {

            throw new IllegalArgumentException(
                    "Source and destination locations cannot be the same"
            );
        }

        // Create new StockTransfer entity
        StockTransfer transfer = new StockTransfer();

        transfer.setSourceLocation(sourceLocation);
        transfer.setDestinationLocation(destinationLocation);
        transfer.setRequestedBy(requestedBy);
        transfer.setNotes(request.getNotes());

        transfer.setStatus(TransferStatus.PENDING);
        transfer.setRequestDate(LocalDateTime.now());

        return stockTransferRepository.save(transfer);
    }

    // Approve transfer
    public StockTransfer approveTransfer(
            Long transferId,
            User approvedBy) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                != TransferStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending transfers can be approved"
            );
        }

        transfer.setStatus(TransferStatus.APPROVED);
        transfer.setApprovedBy(approvedBy);
        transfer.setApprovedDate(LocalDateTime.now());

        return stockTransferRepository.save(transfer);
    }

    // Reject transfer
    public StockTransfer rejectTransfer(Long transferId) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                != TransferStatus.PENDING) {

            throw new IllegalStateException(
                    "Only pending transfers can be rejected"
            );
        }

        transfer.setStatus(TransferStatus.REJECTED);

        return stockTransferRepository.save(transfer);
    }

    // Dispatch transfer
    public StockTransfer dispatchTransfer(Long transferId) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                != TransferStatus.APPROVED) {

            throw new IllegalStateException(
                    "Only approved transfers can be dispatched"
            );
        }

        transfer.setStatus(TransferStatus.DISPATCHED);
        transfer.setDispatchDate(LocalDateTime.now());

        return stockTransferRepository.save(transfer);
    }

    // Mark as in transit
    public StockTransfer markInTransit(Long transferId) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                != TransferStatus.DISPATCHED) {

            throw new IllegalStateException(
                    "Only dispatched transfers can be marked as in transit"
            );
        }

        transfer.setStatus(TransferStatus.IN_TRANSIT);

        return stockTransferRepository.save(transfer);
    }

    // Receive transfer
    public StockTransfer receiveTransfer(Long transferId) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                != TransferStatus.IN_TRANSIT
                &&
                transfer.getStatus()
                        != TransferStatus.DISPATCHED) {

            throw new IllegalStateException(
                    "Transfer must be dispatched before receiving"
            );
        }

        transfer.setStatus(TransferStatus.RECEIVED);
        transfer.setReceivedDate(LocalDateTime.now());

        return stockTransferRepository.save(transfer);
    }

    // Cancel transfer
    public StockTransfer cancelTransfer(
            Long transferId,
            String reason) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                == TransferStatus.DISPATCHED
                ||
                transfer.getStatus()
                        == TransferStatus.IN_TRANSIT
                ||
                transfer.getStatus()
                        == TransferStatus.RECEIVED) {

            throw new IllegalStateException(
                    "Dispatched or received transfers cannot be cancelled"
            );
        }

        transfer.setStatus(TransferStatus.CANCELLED);
        transfer.setCancelReason(reason);

        return stockTransferRepository.save(transfer);
    }

    // Filter by status
    public List<StockTransfer> getTransfersByStatus(
            TransferStatus status) {

        return stockTransferRepository.findByStatus(status);
    }
}