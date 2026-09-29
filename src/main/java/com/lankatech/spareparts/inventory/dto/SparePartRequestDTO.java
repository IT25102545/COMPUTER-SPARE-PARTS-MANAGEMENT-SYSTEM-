package com.lankatech.spareparts.inventory.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SparePartRequestDTO {

    @NotBlank
    private String partCode;

    @NotBlank
    private String partName;

    private String description;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long brandId;

    @NotNull
    @DecimalMin("0.00")
    private BigDecimal unitPrice;

    @NotNull
    @Min(0)
    private Integer reorderLevel;
}
