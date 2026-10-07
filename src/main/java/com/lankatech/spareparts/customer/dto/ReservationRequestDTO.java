package com.lankatech.spareparts.customer.dto;
import jakarta.validation.constraints.*; import lombok.Getter; import lombok.Setter;
@Getter @Setter public class ReservationRequestDTO { @NotNull private Long customerId; @NotNull private Long sparePartId;
    @NotNull private Long locationId; @NotNull @Min(1) private Integer quantity; }
