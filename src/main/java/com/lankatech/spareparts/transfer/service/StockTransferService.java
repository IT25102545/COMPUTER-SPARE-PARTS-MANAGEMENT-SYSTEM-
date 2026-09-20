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
import com.lankatech.spareparts.transfer.dto.ReceiveTransferItemDTO;
import com.lankatech.spareparts.transfer.dto.ReceiveTransferRequestDTO;

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

    public StockTransfer getTransferById(
            Long transferId) {

        return stockTransferRepository
                .findById(transferId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Stock transfer not found: "
                                        + transferId
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
                        .findById(
                                request.getSourceLocationId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Source location not found: "
                                                + request.getSourceLocationId()
                                )
                        );


        // Find destination location
        Location destinationLocation =
                locationRepository
                        .findById(
                                request.getDestinationLocationId()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Destination location not found: "
                                                + request.getDestinationLocationId()
                                )
                        );


        // Find user who created request
        User requestedBy =
                userRepository
                        .findById(
                                request.getRequestedById()
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Requested user not found: "
                                                + request.getRequestedById()
                                )
                        );


        // Source and destination cannot be same
        if (sourceLocation
                .getLocationId()
                .equals(
                        destinationLocation
                                .getLocationId()
                )) {

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
         * Prevent same spare part from being
         * added more than once.
         */
        Set<Long> addedPartIds =
                new HashSet<>();


        // Create transfer items
        for (TransferItemRequestDTO itemRequest :
                request.getItems()) {

            Long sparePartId =
                    itemRequest
                            .getSparePartId();


            // Check duplicate spare part
            if (!addedPartIds.add(
                    sparePartId)) {

                throw new IllegalArgumentException(
                        "Same spare part cannot be added more than once: "
                                + sparePartId
                );
            }


            // Find spare part
            SparePart sparePart =
                    sparePartRepository
                            .findById(
                                    sparePartId
                            )
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
             * addItem() does:
             *
             * 1. Adds item to transfer.items
             * 2. Sets item.stockTransfer = transfer
             */
            transfer.addItem(
                    item
            );
        }


        /*
         * Because StockTransfer.items uses
         * CascadeType.ALL,
         * saving transfer also saves items.
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
                getTransferById(
                        transferId
                );


        if (transfer.getStatus()
                != TransferStatus.PENDING) {

            throw new InvalidTransferStateException(
                    "Only pending transfers can be approved"
            );
        }


        User approvedBy =
                userRepository
                        .findById(
                                approvedById
                        )
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Approved user not found: "
                                                + approvedById
                                )
                        );


        /*
         * Only Inventory Supervisor
         * can approve.
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

    /*
     * Old reject method.
     *
     * Existing automated tests and
     * old compatibility ekata meka thiyenawa.
     */
    public StockTransfer rejectTransfer(
            Long transferId) {

        StockTransfer transfer =
                getTransferById(
                        transferId
                );


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


    /*
     * New reject method with reason.
     *
     * Frontend / controller eken
     * reject reason send karaddi
     * me method eka use wenawa.
     */
    public StockTransfer rejectTransfer(
            Long transferId,
            String reason) {

        StockTransfer transfer =
                getTransferById(
                        transferId
                );


        // Only PENDING transfers can be rejected
        if (transfer.getStatus()
                != TransferStatus.PENDING) {

            throw new InvalidTransferStateException(
                    "Only pending transfers can be rejected"
            );
        }


        // Reject reason is compulsory
        if (reason == null ||
                reason.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Reject reason is required"
            );
        }


        // DB column limit protection
        if (reason.trim().length() > 255) {

            throw new IllegalArgumentException(
                    "Reject reason cannot exceed 255 characters"
            );
        }


        transfer.setStatus(
                TransferStatus.REJECTED
        );


        transfer.setRejectReason(
                reason.trim()
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
                getTransferById(
                        transferId
                );


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
         * Check ALL stock quantities first.
         * No stock changes happen here.
         */
        for (StockTransferItem item :
                transfer.getItems()) {

            Long sparePartId =
                    item
                            .getSparePart()
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
                                                    + item
                                                    .getSparePart()
                                                    .getPartName()
                                    )
                            );


            if (sourceStock.getQuantity()
                    < item.getQuantity()) {

                throw new InsufficientStockException(
                        "Insufficient stock for "
                                + item
                                .getSparePart()
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
         * Now reduce source stock.
         */
        for (StockTransferItem item :
                transfer.getItems()) {

            Long sparePartId =
                    item
                            .getSparePart()
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
                                                    + item
                                                    .getSparePart()
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
                getTransferById(
                        transferId
                );


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

    /*
     * OLD / NORMAL RECEIVE METHOD
     *
     * Existing tests and old controller compatibility
     * ekata me method eka thiyenawa.
     *
     * Discrepancy data send karanne nathnam
     * full quantity received kiyala salakanawa.
     */
    public StockTransfer receiveTransfer(
            Long transferId) {

        StockTransfer transfer =
                getTransferById(
                        transferId
                );


        /*
         * Allowed workflow:
         *
         * DISPATCHED -> RECEIVED
         *
         * OR
         *
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


        for (StockTransferItem item :
                transfer.getItems()) {

            Long sparePartId =
                    item
                            .getSparePart()
                            .getSparePartId();


            Long destinationLocationId =
                    transfer
                            .getDestinationLocation()
                            .getLocationId();


            /*
             * Find destination stock.
             *
             * If destination has never stored
             * the spare part before,
             * create a new Stock row.
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

                                newStock.setQuantity(
                                        0
                                );

                                return newStock;
                            });


            /*
             * No discrepancy data was supplied.
             * Therefore full transferred quantity
             * was received.
             */
            item.setReceivedQuantity(
                    item.getQuantity()
            );


            item.setDiscrepancyNote(
                    null
            );


            // Add full quantity to destination
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


    /*
     * NEW RECEIVE METHOD
     * WITH DISCREPANCY SUPPORT
     */
    public StockTransfer receiveTransfer(
            Long transferId,
            ReceiveTransferRequestDTO request) {

        StockTransfer transfer =
                getTransferById(
                        transferId
                );


        // Only dispatched / in-transit transfers can be received
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


        // Request must contain received item details
        if (request == null ||
                request.getItems() == null ||
                request.getItems().isEmpty()) {

            throw new IllegalArgumentException(
                    "Received item details are required"
            );
        }


        /*
         * Received details must be supplied
         * for every transfer item.
         */
        if (request.getItems().size()
                != transfer.getItems().size()) {

            throw new IllegalArgumentException(
                    "Received details must be provided for every transfer item"
            );
        }


        Set<Long> processedItemIds =
                new HashSet<>();


        /*
         * FIRST LOOP
         *
         * Validate everything BEFORE
         * changing destination stock.
         */
        for (ReceiveTransferItemDTO receivedItem :
                request.getItems()) {

            Long transferItemId =
                    receivedItem
                            .getTransferItemId();


            if (transferItemId == null) {

                throw new IllegalArgumentException(
                        "Transfer item ID is required"
                );
            }


            // Same transfer item cannot appear twice
            if (!processedItemIds.add(
                    transferItemId)) {

                throw new IllegalArgumentException(
                        "Duplicate transfer item: "
                                + transferItemId
                );
            }


            StockTransferItem item =
                    transfer
                            .getItems()
                            .stream()
                            .filter(existingItem ->
                                    transferItemId.equals(
                                            existingItem
                                                    .getTransferItemId()
                                    )
                            )
                            .findFirst()
                            .orElseThrow(() ->
                                    new IllegalArgumentException(
                                            "Transfer item does not belong to this transfer: "
                                                    + transferItemId
                                    )
                            );


            Integer receivedQuantity =
                    receivedItem
                            .getReceivedQuantity();


            if (receivedQuantity == null ||
                    receivedQuantity < 0) {

                throw new IllegalArgumentException(
                        "Received quantity cannot be negative"
                );
            }


            /*
             * Cannot receive more than
             * transferred quantity.
             */
            if (receivedQuantity >
                    item.getQuantity()) {

                throw new IllegalArgumentException(
                        "Received quantity cannot exceed transferred quantity for "
                                + item
                                .getSparePart()
                                .getPartName()
                );
            }


            /*
             * If received quantity is different,
             * discrepancy reason is required.
             */
            if (!receivedQuantity.equals(
                    item.getQuantity())) {

                String note =
                        receivedItem
                                .getDiscrepancyNote();


                if (note == null ||
                        note.trim().isEmpty()) {

                    throw new IllegalArgumentException(
                            "Discrepancy note is required when received quantity differs for "
                                    + item
                                    .getSparePart()
                                    .getPartName()
                    );
                }
            }
        }


        /*
         * SECOND LOOP
         *
         * All validation passed.
         * Now update destination stock
         * and discrepancy information.
         */
        for (ReceiveTransferItemDTO receivedItem :
                request.getItems()) {

            StockTransferItem item =
                    transfer
                            .getItems()
                            .stream()
                            .filter(existingItem ->
                                    receivedItem
                                            .getTransferItemId()
                                            .equals(
                                                    existingItem
                                                            .getTransferItemId()
                                            )
                            )
                            .findFirst()
                            .orElseThrow();


            Integer receivedQuantity =
                    receivedItem
                            .getReceivedQuantity();


            Long sparePartId =
                    item
                            .getSparePart()
                            .getSparePartId();


            Long destinationLocationId =
                    transfer
                            .getDestinationLocation()
                            .getLocationId();


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

                                newStock.setQuantity(
                                        0
                                );

                                return newStock;
                            });


            // Save actual received quantity
            item.setReceivedQuantity(
                    receivedQuantity
            );


            /*
             * Exact quantity received:
             * no discrepancy.
             */
            if (receivedQuantity.equals(
                    item.getQuantity())) {

                item.setDiscrepancyNote(
                        null
                );
            }

            /*
             * Quantity differs:
             * save discrepancy note.
             */
            else {

                item.setDiscrepancyNote(
                        receivedItem
                                .getDiscrepancyNote()
                                .trim()
                );
            }


            /*
             * IMPORTANT:
             *
             * Destination stock receives
             * actual received quantity,
             * NOT requested quantity.
             */
            destinationStock.setQuantity(
                    destinationStock.getQuantity()
                            + receivedQuantity
            );


            stockRepository.save(
                    destinationStock
            );
        }


        // Mark transfer as received
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
                getTransferById(
                        transferId
                );


        /*
         * Only transfers which have NOT
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