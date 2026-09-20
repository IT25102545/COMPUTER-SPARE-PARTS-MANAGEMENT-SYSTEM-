package com.lankatech.spareparts.transfer.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lankatech.spareparts.inventory.entity.SparePart;
import jakarta.persistence.*;

@Entity
@Table(name = "stock_transfer_items")
public class StockTransferItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "transfer_item_id")
    private Long transferItemId;

    // Me item eka mona stock transfer ekatada belong wenne kiyala connect karanawa
    @ManyToOne(optional = false)
    @JoinColumn(name = "transfer_id", nullable = false)
    @JsonIgnore
    private StockTransfer stockTransfer;

    // Transfer karana actual spare part eka
    @ManyToOne(optional = false)
    @JoinColumn(name = "spare_part_id", nullable = false)
    private SparePart sparePart;

    // Transfer karana quantity eka
    @Column(nullable = false)
    private Integer quantity;

    public Long getTransferItemId() {
        return transferItemId;
    }

    public void setTransferItemId(Long transferItemId) {
        this.transferItemId = transferItemId;
    }

    public StockTransfer getStockTransfer() {
        return stockTransfer;
    }

    public void setStockTransfer(StockTransfer stockTransfer) {
        this.stockTransfer = stockTransfer;
    }

    public SparePart getSparePart() {
        return sparePart;
    }

    public void setSparePart(SparePart sparePart) {
        this.sparePart = sparePart;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}