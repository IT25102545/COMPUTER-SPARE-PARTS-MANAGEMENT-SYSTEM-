package com.lankatech.spareparts.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RejectTransferRequestDTO {

    @NotBlank(message = "Reject reason is required")
    @Size(max = 255, message = "Reject reason cannot exceed 255 characters")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}