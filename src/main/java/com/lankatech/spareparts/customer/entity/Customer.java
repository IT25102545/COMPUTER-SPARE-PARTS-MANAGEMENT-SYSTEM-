package com.lankatech.spareparts.customer.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
import jakarta.validation.constraints.*;
@Entity @Table(name="customers") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="customer_id") private Long customerId;
    @NotBlank(message = "Customer name is required")
    @Size(max = 120, message = "Customer name must be 120 characters or fewer")
    @Column(name="customer_name",nullable=false,length=120) private String customerName;
    @Size(max = 30, message = "Phone must be 30 characters or fewer")
    @Column(length=30) private String phone;
    @Email(message = "Email must be valid")
    @Size(max = 120, message = "Email must be 120 characters or fewer")
    @Column(length=120) private String email;
    @Size(max = 255, message = "Address must be 255 characters or fewer")
    @Column(length=255) private String address;
    @Column(name="created_at") private LocalDateTime createdAt;
    @PrePersist public void onCreate(){if(createdAt==null)createdAt=LocalDateTime.now();}
}