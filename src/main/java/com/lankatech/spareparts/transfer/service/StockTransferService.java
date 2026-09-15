package com.lankatech.spareparts.transfer.service;

import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.auth.repository.UserRepository;
import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.common.repository.LocationRepository;
import com.lankatech.spareparts.transfer.dto.StockTransferRequestDTO;
import com.lankatech.spareparts.transfer.entity.StockTransfer;
import com.lankatech.spareparts.transfer.enums.TransferStatus;
import com.lankatech.spareparts.transfer.exception.InsufficientStockException;
import com.lankatech.spareparts.transfer.exception.InvalidTransferStateException;
import com.lankatech.spareparts.transfer.exception.ResourceNotFoundException;
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
                        new ResourceNotFoundException(
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
                                new ResourceNotFoundException(
                                        "Source location not found: "
                                                + request.getSourceLocationId()
                                )
                        );

        // Find destination location
        Location destinationLocation =
                locationRepository
                        .findById(request.getDestinationLocationId())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Destination location not found: "
                                                + request.getDestinationLocationId()
                                )
                        );

        // Find user who created request
        User requestedBy =
                userRepository
                        .findById(request.getRequestedById())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Requested user not found: "
                                                + request.getRequestedById()
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
            Long approvedById) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                != TransferStatus.PENDING) {

            throw new InvalidTransferStateException(
                    "Only pending transfers can be approved"
            );
        }

        User approvedBy =
                userRepository
                        .findById(approvedById)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Approved user not found: "
                                                + approvedById
                                )
                        );

        if (approvedBy.getRole() == null ||
                !"INVENTORY_SUPERVISOR"
                        .equalsIgnoreCase(
                                approvedBy
                                        .getRole()
                                        .getRoleName()
                        )) {

            throw new IllegalArgumentException(
                    "Only an Inventory Supervisor can approve transfers"
            );
        }

        transfer.setStatus(
                TransferStatus.APPROVED
        );

        transfer.setApprovedBy(
                approvedBy
        );

        transfer.setApprovedDate(
                LocalDateTime.now()
        );

        return stockTransferRepository
                .save(transfer);
    }

    // Reject transfer
    public StockTransfer rejectTransfer(Long transferId) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                != TransferStatus.PENDING) {

            throw new InvalidTransferStateException(
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

            throw new InvalidTransferStateException(
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

            throw new InvalidTransferStateException(
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

            throw new InvalidTransferStateException(
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

        if (transfer.getStatus() != TransferStatus.PENDING &&
                transfer.getStatus() != TransferStatus.APPROVED) {

            throw new InvalidTransferStateException(
                    "Only pending or approved transfers can be cancelled"
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