package com.lankatech.spareparts.customer.entity;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="customers") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Customer {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="customer_id") private Long customerId;
    @Column(name="customer_name",nullable=false,length=120) private String customerName;
    @Column(length=30) private String phone;
    @Column(length=120) private String email;
    @Column(length=255) private String address;
    @Column(name="created_at") private LocalDateTime createdAt;
    @PrePersist public void onCreate(){if(createdAt==null)createdAt=LocalDateTime.now();}
}