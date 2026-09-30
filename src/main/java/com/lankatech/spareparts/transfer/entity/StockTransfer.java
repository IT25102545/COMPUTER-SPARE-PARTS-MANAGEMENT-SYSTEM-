package com.lankatech.spareparts.transfer.entity;

import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.transfer.enums.TransferStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stock_transfers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class StockTransfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_id")
    private Long transferId;

    @ManyToOne
    @JoinColumn(name = "source_location_id", nullable = false)
    private Location sourceLocation;

    @ManyToOne
    @JoinColumn(name = "destination_location_id", nullable = false)
    private Location destinationLocation;

    @ManyToOne
    @JoinColumn(name = "requested_by", nullable = false)
    private User requestedBy;

    @ManyToOne
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "request_date", nullable = false)
    private LocalDateTime requestDate;

    @Column(name = "approved_date")
    private LocalDateTime approvedDate;

    @Column(name = "dispatch_date")
    private LocalDateTime dispatchDate;

    @Column(name = "received_date")
    private LocalDateTime receivedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TransferStatus status;

    @Column(length = 500)
    private String notes;

    // Transfer reject karapu reason eka
    @Column(name = "reject_reason", length = 255)
    private String rejectReason;

    // Transfer cancel karapu reason eka
    @Column(name = "cancel_reason", length = 255)
    private String cancelReason;

    @OneToMany(
            mappedBy = "stockTransfer",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<StockTransferItem> items = new ArrayList<>();

    public void addItem(StockTransferItem item) {
        items.add(item);
        item.setStockTransfer(this);
    }

    public void removeItem(StockTransferItem item) {
        items.remove(item);
        item.setStockTransfer(null);
    }

    @PrePersist
    public void onCreate() {

        if (requestDate == null) {
            requestDate = LocalDateTime.now();
        }

        if (status == null) {
            status = TransferStatus.PENDING;
        }
    }
}