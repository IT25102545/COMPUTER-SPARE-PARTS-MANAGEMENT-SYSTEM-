package com.lankatech.spareparts.transfer.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class ReceiveTransferRequestDTO {

    @NotEmpty(message = "At least one received item is required")
    @Valid
    private List<ReceiveTransferItemDTO> items;


    public List<ReceiveTransferItemDTO> getItems() {
        return items;
    }

    public void setItems(List<ReceiveTransferItemDTO> items) {
        this.items = items;
    }
}