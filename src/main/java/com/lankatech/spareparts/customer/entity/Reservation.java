package com.lankatech.spareparts.customer.entity;
import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.inventory.entity.SparePart;
import jakarta.persistence.*; import lombok.*; import java.time.LocalDateTime;
@Entity @Table(name="reservations") @Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Reservation {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) @Column(name="reservation_id") private Long reservationId;
    @ManyToOne @JoinColumn(name="customer_id",nullable=false) private Customer customer;
    @ManyToOne @JoinColumn(name="spare_part_id",nullable=false) private SparePart sparePart;
    @ManyToOne @JoinColumn(name="location_id",nullable=false) private Location location;
    @Column(nullable=false) private Integer quantity;
    @Column(nullable=false,length=30) private String status;
    @Column(name="reserved_at") private LocalDateTime reservedAt;
    @PrePersist public void onCreate(){if(status==null)status="ACTIVE";if(reservedAt==null)reservedAt=LocalDateTime.now();}
}