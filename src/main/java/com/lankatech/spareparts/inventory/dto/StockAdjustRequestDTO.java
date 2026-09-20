package com.lankatech.spareparts.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StockAdjustRequestDTO {

    @NotNull
    private Long sparePartId;

    @NotNull
    private Long locationId;

    @NotNull
    private Integer quantityChange;
}