package com.lankatech.spareparts.sales.dto;

import com.lankatech.spareparts.sales.entity.PaymentMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class SaleRequestDTO {

    private Long customerId;

    @NotNull
    private Long locationId;

    @NotNull
    private Long cashierUserId;

    @NotNull
    private PaymentMethod paymentMethod;

    @NotEmpty
    @Valid
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {

        @NotNull
        private Long sparePartId;

        @NotNull
        @Min(1)
        private Integer quantity;
    }
}