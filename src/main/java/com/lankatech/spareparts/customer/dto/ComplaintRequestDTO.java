package com.lankatech.spareparts.customer.dto;
import jakarta.validation.constraints.*; import lombok.Getter; import lombok.Setter;
@Getter @Setter public class ComplaintRequestDTO { @NotNull private Long customerId; @NotBlank private String subject;
    @NotBlank private String description; }
