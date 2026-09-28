package com.lankatech.spareparts.transfer.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ReceiveTransferItemDTO {

    @NotNull(message = "Transfer item ID is required")
    private Long transferItemId;

    @NotNull(message = "Received quantity is required")
    @Min(value = 0, message = "Received quantity cannot be negative")
    private Integer receivedQuantity;

    private String discrepancyNote;


    public Long getTransferItemId() {
        return transferItemId;
    }

    public void setTransferItemId(Long transferItemId) {
        this.transferItemId = transferItemId;
    }

    public Integer getReceivedQuantity() {
        return receivedQuantity;
    }

    public void setReceivedQuantity(Integer receivedQuantity) {
        this.receivedQuantity = receivedQuantity;
    }

    public String getDiscrepancyNote() {
        return discrepancyNote;
    }

    public void setDiscrepancyNote(String discrepancyNote) {
        this.discrepancyNote = discrepancyNote;
    }
}