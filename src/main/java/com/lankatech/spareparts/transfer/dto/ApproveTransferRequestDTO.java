package com.lankatech.spareparts.transfer.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApproveTransferRequestDTO {

    @NotNull(message = "Approved user ID is required")
    private Long approvedById;
}