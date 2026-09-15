package com.lankatech.spareparts.transfer.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockTransferRequestDTO {

    @NotNull(message = "Source location is required")
    private Long sourceLocationId;

    @NotNull(message = "Destination location is required")
    private Long destinationLocationId;

    @NotNull(message = "Requested user is required")
    private Long requestedById;

    @Size(max = 500, message = "Notes cannot exceed 500 characters")
    private String notes;
}