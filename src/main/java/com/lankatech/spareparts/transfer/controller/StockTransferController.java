package com.lankatech.spareparts.transfer.controller;

import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.transfer.dto.StockTransferRequestDTO;
import com.lankatech.spareparts.transfer.entity.StockTransfer;
import com.lankatech.spareparts.transfer.enums.TransferStatus;
import com.lankatech.spareparts.transfer.service.StockTransferService;
import com.lankatech.spareparts.transfer.dto.ApproveTransferRequestDTO;
import jakarta.validation.Valid;
import com.lankatech.spareparts.transfer.dto.CancelTransferRequestDTO;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/stock-transfers")
public class StockTransferController {

    private final StockTransferService stockTransferService;

    public StockTransferController(StockTransferService stockTransferService) {
        this.stockTransferService = stockTransferService;
    }

    // Get all stock transfer requests
    @GetMapping
    public List<StockTransfer> getAllTransfers() {
        return stockTransferService.getAllTransfers();
    }

    // Get one stock transfer using transfer ID
    @GetMapping("/{id}")
    public StockTransfer getTransferById(@PathVariable Long id) {
        return stockTransferService.getTransferById(id);
    }

    // Get transfers according to status
    @GetMapping("/status/{status}")
    public List<StockTransfer> getTransfersByStatus(
            @PathVariable TransferStatus status) {

        return stockTransferService.getTransfersByStatus(status);
    }

    // Create new stock transfer request
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public StockTransfer createTransfer(
            @Valid @RequestBody StockTransferRequestDTO request) {
        return stockTransferService.createTransfer(request);
    }

    // Approve pending stock transfer
    @PutMapping("/{id}/approve")
    public StockTransfer approveTransfer(
            @PathVariable Long id,
            @Valid @RequestBody ApproveTransferRequestDTO request) {

        return stockTransferService.approveTransfer(
                id,
                request.getApprovedById()
        );
    }

    // Reject pending stock transfer
    @PutMapping("/{id}/reject")
    public StockTransfer rejectTransfer(@PathVariable Long id) {

        return stockTransferService.rejectTransfer(id);
    }

    // Dispatch approved stock transfer
    @PutMapping("/{id}/dispatch")
    public StockTransfer dispatchTransfer(@PathVariable Long id) {

        return stockTransferService.dispatchTransfer(id);
    }

    // Mark dispatched transfer as in transit
    @PutMapping("/{id}/in-transit")
    public StockTransfer markInTransit(@PathVariable Long id) {

        return stockTransferService.markInTransit(id);
    }

    // Mark transfer as received
    @PutMapping("/{id}/receive")
    public StockTransfer receiveTransfer(@PathVariable Long id) {

        return stockTransferService.receiveTransfer(id);
    }

    // Cancel stock transfer
    @PutMapping("/{id}/cancel")
    public StockTransfer cancelTransfer(
            @PathVariable Long id,
            @Valid @RequestBody CancelTransferRequestDTO request) {

        return stockTransferService.cancelTransfer(
                id,
                request.getReason()
        );
    }
}