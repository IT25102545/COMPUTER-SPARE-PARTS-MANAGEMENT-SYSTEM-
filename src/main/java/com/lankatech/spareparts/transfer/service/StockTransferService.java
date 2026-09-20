package com.lankatech.spareparts.transfer.service;

import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.auth.repository.UserRepository;
import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.common.repository.LocationRepository;

import com.lankatech.spareparts.inventory.entity.SparePart;
import com.lankatech.spareparts.inventory.entity.Stock;
import com.lankatech.spareparts.inventory.repository.SparePartRepository;
import com.lankatech.spareparts.inventory.repository.StockRepository;

import com.lankatech.spareparts.transfer.dto.StockTransferRequestDTO;
import com.lankatech.spareparts.transfer.dto.TransferItemRequestDTO;
import com.lankatech.spareparts.transfer.entity.StockTransfer;
import com.lankatech.spareparts.transfer.entity.StockTransferItem;
import com.lankatech.spareparts.transfer.enums.TransferStatus;
import com.lankatech.spareparts.transfer.exception.InsufficientStockException;
import com.lankatech.spareparts.transfer.exception.InvalidTransferStateException;
import com.lankatech.spareparts.transfer.exception.ResourceNotFoundException;
import com.lankatech.spareparts.transfer.repository.StockTransferRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class StockTransferService {

    private final StockTransferRepository stockTransferRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;

    // Inventory repositories
    private final SparePartRepository sparePartRepository;
    private final StockRepository stockRepository;

    public StockTransferService(
            StockTransferRepository stockTransferRepository,
            LocationRepository locationRepository,
            UserRepository userRepository,
            SparePartRepository sparePartRepository,
            StockRepository stockRepository) {

        this.stockTransferRepository = stockTransferRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.sparePartRepository = sparePartRepository;
        this.stockRepository = stockRepository;
    }

    // =========================================================
    // GET ALL TRANSFERS
    // =========================================================

    public List<StockTransfer> getAllTransfers() {

        return stockTransferRepository
                .findAllByOrderByRequestDateDesc();
    }

    // =========================================================
    // GET ONE TRANSFER BY ID
    // =========================================================

    public StockTransfer getTransferById(Long transferId) {

        return stockTransferRepository
                .findById(transferId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock transfer not found: " + transferId
                        )
                );
    }

    // =========================================================
    // CREATE NEW TRANSFER
    // =========================================================

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

        // Find user who created the request
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

        // Create main transfer
        StockTransfer transfer =
                new StockTransfer();

        transfer.setSourceLocation(
                sourceLocation
        );

        transfer.setDestinationLocation(
                destinationLocation
        );

        transfer.setRequestedBy(
                requestedBy
        );

        transfer.setNotes(
                request.getNotes()
        );

        transfer.setStatus(
                TransferStatus.PENDING
        );

        transfer.setRequestDate(
                LocalDateTime.now()
        );

        /*
         * Prevent same spare part from being added
         * more than once in the same transfer.
         */
        Set<Long> addedPartIds =
                new HashSet<>();

        // Create transfer items
        for (TransferItemRequestDTO itemRequest :
                request.getItems()) {

            Long sparePartId =
                    itemRequest.getSparePartId();

            // Check duplicate spare part
            if (!addedPartIds.add(sparePartId)) {

                throw new IllegalArgumentException(
                        "Same spare part cannot be added more than once: "
                                + sparePartId
                );
            }

            // Find spare part
            SparePart sparePart =
                    sparePartRepository
                            .findById(sparePartId)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Spare part not found: "
                                                    + sparePartId
                                    )
                            );

            // Prevent inactive spare parts
            if (Boolean.FALSE.equals(
                    sparePart.getActive())) {

                throw new IllegalArgumentException(
                        "Inactive spare part cannot be transferred: "
                                + sparePart.getPartName()
                );
            }

            // Create transfer item
            StockTransferItem item =
                    new StockTransferItem();

            item.setSparePart(
                    sparePart
            );

            item.setQuantity(
                    itemRequest.getQuantity()
            );

            /*
             * addItem() does two things:
             * 1. Adds item to transfer.items
             * 2. Sets item.stockTransfer = transfer
             */
            transfer.addItem(item);
        }

        /*
         * Because StockTransfer.items uses
         * cascade = CascadeType.ALL,
         * saving the transfer also saves its items.
         */
        return stockTransferRepository
                .save(transfer);
    }

    // =========================================================
    // APPROVE TRANSFER
    // =========================================================

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

        /*
         * Only Inventory Supervisor can approve.
         */
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

    // =========================================================
    // REJECT TRANSFER
    // =========================================================

    public StockTransfer rejectTransfer(
            Long transferId) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                != TransferStatus.PENDING) {

            throw new InvalidTransferStateException(
                    "Only pending transfers can be rejected"
            );
        }

        transfer.setStatus(
                TransferStatus.REJECTED
        );

        return stockTransferRepository
                .save(transfer);
    }

    // =========================================================
    // DISPATCH TRANSFER
    // SOURCE STOCK WILL BE REDUCED HERE
    // =========================================================

    public StockTransfer dispatchTransfer(
            Long transferId) {

        StockTransfer transfer =
                getTransferById(transferId);

        // Only approved transfers can be dispatched
        if (transfer.getStatus()
                != TransferStatus.APPROVED) {

            throw new InvalidTransferStateException(
                    "Only approved transfers can be dispatched"
            );
        }

        // Transfer must contain items
        if (transfer.getItems() == null ||
                transfer.getItems().isEmpty()) {

            throw new InvalidTransferStateException(
                    "Transfer does not contain any items"
            );
        }

        /*
         * FIRST LOOP
         *
         * Check ALL stock quantities before
         * changing any stock.
         */
        for (StockTransferItem item :
                transfer.getItems()) {

            Long sparePartId =
                    item.getSparePart()
                            .getSparePartId();

            Long sourceLocationId =
                    transfer
                            .getSourceLocation()
                            .getLocationId();

            Stock sourceStock =
                    stockRepository
                            .findBySparePartSparePartIdAndLocationLocationId(
                                    sparePartId,
                                    sourceLocationId
                            )
                            .orElseThrow(() ->
                                    new InsufficientStockException(
                                            "No stock available at source location for "
                                                    + item.getSparePart()
                                                    .getPartName()
                                    )
                            );

            if (sourceStock.getQuantity()
                    < item.getQuantity()) {

                throw new InsufficientStockException(
                        "Insufficient stock for "
                                + item.getSparePart()
                                .getPartName()
                                + ". Available: "
                                + sourceStock.getQuantity()
                                + ", Requested: "
                                + item.getQuantity()
                );
            }
        }

        /*
         * SECOND LOOP
         *
         * All stock checks passed.
         * Now reduce stock from source location.
         */
        for (StockTransferItem item :
                transfer.getItems()) {

            Long sparePartId =
                    item.getSparePart()
                            .getSparePartId();

            Long sourceLocationId =
                    transfer
                            .getSourceLocation()
                            .getLocationId();

            Stock sourceStock =
                    stockRepository
                            .findBySparePartSparePartIdAndLocationLocationId(
                                    sparePartId,
                                    sourceLocationId
                            )
                            .orElseThrow(() ->
                                    new InsufficientStockException(
                                            "Source stock record not found for "
                                                    + item.getSparePart()
                                                    .getPartName()
                                    )
                            );

            // Deduct transferred quantity
            sourceStock.setQuantity(
                    sourceStock.getQuantity()
                            - item.getQuantity()
            );

            stockRepository.save(
                    sourceStock
            );
        }

        // Change transfer status
        transfer.setStatus(
                TransferStatus.DISPATCHED
        );

        transfer.setDispatchDate(
                LocalDateTime.now()
        );

        return stockTransferRepository
                .save(transfer);
    }

    // =========================================================
    // MARK AS IN TRANSIT
    // =========================================================

    public StockTransfer markInTransit(
            Long transferId) {

        StockTransfer transfer =
                getTransferById(transferId);

        if (transfer.getStatus()
                != TransferStatus.DISPATCHED) {

            throw new InvalidTransferStateException(
                    "Only dispatched transfers can be marked as in transit"
            );
        }

        transfer.setStatus(
                TransferStatus.IN_TRANSIT
        );

        return stockTransferRepository
                .save(transfer);
    }

    // =========================================================
    // RECEIVE TRANSFER
    // DESTINATION STOCK WILL BE INCREASED HERE
    // =========================================================

    public StockTransfer receiveTransfer(
            Long transferId) {

        StockTransfer transfer =
                getTransferById(transferId);

        /*
         * Current workflow allows:
         *
         * DISPATCHED -> RECEIVED
         * OR
         * DISPATCHED -> IN_TRANSIT -> RECEIVED
         */
        if (transfer.getStatus()
                != TransferStatus.IN_TRANSIT
                &&
                transfer.getStatus()
                        != TransferStatus.DISPATCHED) {

            throw new InvalidTransferStateException(
                    "Transfer must be dispatched before receiving"
            );
        }

        // Transfer must contain items
        if (transfer.getItems() == null ||
                transfer.getItems().isEmpty()) {

            throw new InvalidTransferStateException(
                    "Transfer does not contain any items"
            );
        }

        /*
         * Add each transferred item quantity
         * to destination stock.
         */
        for (StockTransferItem item :
                transfer.getItems()) {

            Long sparePartId =
                    item.getSparePart()
                            .getSparePartId();

            Long destinationLocationId =
                    transfer
                            .getDestinationLocation()
                            .getLocationId();

            /*
             * Find destination stock.
             *
             * If the destination location has never
             * stored this spare part before,
             * create a new Stock row with quantity 0.
             */
            Stock destinationStock =
                    stockRepository
                            .findBySparePartSparePartIdAndLocationLocationId(
                                    sparePartId,
                                    destinationLocationId
                            )
                            .orElseGet(() -> {

                                Stock newStock =
                                        new Stock();

                                newStock.setSparePart(
                                        item.getSparePart()
                                );

                                newStock.setLocation(
                                        transfer
                                                .getDestinationLocation()
                                );

                                newStock.setQuantity(0);

                                return newStock;
                            });

            // Add transferred quantity
            destinationStock.setQuantity(
                    destinationStock.getQuantity()
                            + item.getQuantity()
            );

            stockRepository.save(
                    destinationStock
            );
        }

        transfer.setStatus(
                TransferStatus.RECEIVED
        );

        transfer.setReceivedDate(
                LocalDateTime.now()
        );

        return stockTransferRepository
                .save(transfer);
    }

    // =========================================================
    // CANCEL TRANSFER
    // =========================================================

    public StockTransfer cancelTransfer(
            Long transferId,
            String reason) {

        StockTransfer transfer =
                getTransferById(transferId);

        /*
         * Only transfers that have NOT yet
         * been dispatched can be cancelled.
         */
        if (transfer.getStatus()
                != TransferStatus.PENDING
                &&
                transfer.getStatus()
                        != TransferStatus.APPROVED) {

            throw new InvalidTransferStateException(
                    "Only pending or approved transfers can be cancelled"
            );
        }

        transfer.setStatus(
                TransferStatus.CANCELLED
        );

        transfer.setCancelReason(
                reason
        );

        return stockTransferRepository
                .save(transfer);
    }

    // =========================================================
    // FILTER BY STATUS
    // =========================================================

    public List<StockTransfer> getTransfersByStatus(
            TransferStatus status) {

        return stockTransferRepository
                .findByStatus(status);
    }
}