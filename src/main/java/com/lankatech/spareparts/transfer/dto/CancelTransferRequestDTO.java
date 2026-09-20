package com.lankatech.spareparts.transfer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelTransferRequestDTO {

    @NotBlank(message = "Cancel reason is required")
    @Size(max = 255, message = "Cancel reason cannot exceed 255 characters")
    private String reason;
}