package com.lankatech.spareparts.transfer.controller;

import com.lankatech.spareparts.transfer.dto.ApproveTransferRequestDTO;
import com.lankatech.spareparts.transfer.dto.CancelTransferRequestDTO;
import com.lankatech.spareparts.transfer.dto.ReceiveTransferRequestDTO;
import com.lankatech.spareparts.transfer.dto.RejectTransferRequestDTO;
import com.lankatech.spareparts.transfer.dto.StockTransferRequestDTO;

import com.lankatech.spareparts.transfer.entity.StockTransfer;
import com.lankatech.spareparts.transfer.enums.TransferStatus;
import com.lankatech.spareparts.transfer.service.StockTransferService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/stock-transfers")
public class StockTransferController {

    private final StockTransferService stockTransferService;


    public StockTransferController(
            StockTransferService stockTransferService) {

        this.stockTransferService =
                stockTransferService;
    }


    // =========================================================
    // GET ALL STOCK TRANSFERS
    // =========================================================

    @GetMapping
    public List<StockTransfer> getAllTransfers() {

        return stockTransferService
                .getAllTransfers();
    }


    // =========================================================
    // GET ONE STOCK TRANSFER BY ID
    // =========================================================

    @GetMapping("/{id}")
    public StockTransfer getTransferById(
            @PathVariable Long id) {

        return stockTransferService
                .getTransferById(id);
    }


    // =========================================================
    // GET TRANSFERS BY STATUS
    // =========================================================

    @GetMapping("/status/{status}")
    public List<StockTransfer> getTransfersByStatus(
            @PathVariable TransferStatus status) {

        return stockTransferService
                .getTransfersByStatus(status);
    }


    // =========================================================
    // CREATE NEW STOCK TRANSFER
    // =========================================================

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockTransfer createTransfer(
            @Valid
            @RequestBody
            StockTransferRequestDTO request) {

        return stockTransferService
                .createTransfer(request);
    }


    // =========================================================
    // APPROVE PENDING TRANSFER
    // =========================================================

    @PutMapping("/{id}/approve")
    public StockTransfer approveTransfer(
            @PathVariable Long id,
            @Valid
            @RequestBody
            ApproveTransferRequestDTO request) {

        return stockTransferService
                .approveTransfer(
                        id,
                        request.getApprovedById()
                );
    }


    // =========================================================
    // REJECT PENDING TRANSFER WITH REASON
    // =========================================================

    @PutMapping("/{id}/reject")
    public StockTransfer rejectTransfer(
            @PathVariable Long id,
            @Valid
            @RequestBody
            RejectTransferRequestDTO request) {

        return stockTransferService
                .rejectTransfer(
                        id,
                        request.getReason()
                );
    }


    // =========================================================
    // DISPATCH APPROVED TRANSFER
    // =========================================================

    @PutMapping("/{id}/dispatch")
    public StockTransfer dispatchTransfer(
            @PathVariable Long id) {

        return stockTransferService
                .dispatchTransfer(id);
    }


    // =========================================================
    // MARK DISPATCHED TRANSFER AS IN TRANSIT
    // =========================================================

    @PutMapping("/{id}/in-transit")
    public StockTransfer markInTransit(
            @PathVariable Long id) {

        return stockTransferService
                .markInTransit(id);
    }


    // =========================================================
    // RECEIVE TRANSFER
    //
    // Supports:
    //
    // 1. Old normal receive without JSON body
    // 2. New receive with actual quantities
    //    and discrepancy information
    // =========================================================

    @PutMapping("/{id}/receive")
    public StockTransfer receiveTransfer(
            @PathVariable Long id,
            @Valid
            @RequestBody(required = false)
            ReceiveTransferRequestDTO request) {

        /*
         * No JSON body supplied:
         * normal receive.
         */
        if (request == null) {

            return stockTransferService
                    .receiveTransfer(id);
        }


        /*
         * JSON body supplied:
         * receive with actual quantities
         * and discrepancy information.
         */
        return stockTransferService
                .receiveTransfer(
                        id,
                        request
                );
    }


    // =========================================================
    // CANCEL TRANSFER WITH REASON
    // =========================================================

    @PutMapping("/{id}/cancel")
    public StockTransfer cancelTransfer(
            @PathVariable Long id,
            @Valid
            @RequestBody
            CancelTransferRequestDTO request) {

        return stockTransferService
                .cancelTransfer(
                        id,
                        request.getReason()
                );
    }
}