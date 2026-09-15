package com.lankatech.spareparts.supplier.service;

import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.auth.repository.UserRepository;
import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.common.repository.LocationRepository;
import com.lankatech.spareparts.inventory.entity.SparePart;
import com.lankatech.spareparts.inventory.entity.Stock;
import com.lankatech.spareparts.inventory.repository.SparePartRepository;
import com.lankatech.spareparts.inventory.repository.StockRepository;
import com.lankatech.spareparts.supplier.dto.PurchaseOrderRequestDTO;
import com.lankatech.spareparts.supplier.entity.PurchaseOrder;
import com.lankatech.spareparts.supplier.entity.PurchaseOrderItem;
import com.lankatech.spareparts.supplier.entity.PurchaseOrderStatus;
import com.lankatech.spareparts.supplier.entity.Supplier;
import com.lankatech.spareparts.supplier.repository.PurchaseOrderRepository;
import com.lankatech.spareparts.supplier.repository.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class SupplierPurchaseService {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository poRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final SparePartRepository sparePartRepository;
    private final StockRepository stockRepository;

    public SupplierPurchaseService(
            SupplierRepository supplierRepository,
            PurchaseOrderRepository poRepository,
            LocationRepository locationRepository,
            UserRepository userRepository,
            SparePartRepository sparePartRepository,
            StockRepository stockRepository
    ) {
        this.supplierRepository = supplierRepository;
        this.poRepository = poRepository;
        this.locationRepository = locationRepository;
        this.userRepository = userRepository;
        this.sparePartRepository = sparePartRepository;
        this.stockRepository = stockRepository;
    }

    public List<Supplier> getSuppliers() {
        return supplierRepository.findAll();
    }

    public Supplier createSupplier(Supplier supplier) {
        supplier.setSupplierId(null);
        return supplierRepository.save(supplier);
    }

    public List<PurchaseOrder> getOrders() {
        return poRepository.findAll();
    }

    public PurchaseOrder createOrder(PurchaseOrderRequestDTO request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        User user = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setLocation(location);
        purchaseOrder.setCreatedBy(user);
        purchaseOrder.setStatus(PurchaseOrderStatus.ORDERED);

        BigDecimal total = BigDecimal.ZERO;
        for (PurchaseOrderRequestDTO.Item itemRequest : request.getItems()) {
            SparePart part = sparePartRepository.findById(itemRequest.getSparePartId())
                    .orElseThrow(() -> new IllegalArgumentException("Part not found"));

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(purchaseOrder);
            item.setSparePart(part);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitCost(itemRequest.getUnitCost());
            item.setReceivedQuantity(0);
            purchaseOrder.getItems().add(item);

            total = total.add(itemRequest.getUnitCost().multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        purchaseOrder.setTotalAmount(total);
        return poRepository.save(purchaseOrder);
    }

    public PurchaseOrder receive(Long id) {
        PurchaseOrder purchaseOrder = poRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PO not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new IllegalStateException("Only ordered PO can be received");
        }

        for (PurchaseOrderItem item : purchaseOrder.getItems()) {
            Stock stock = stockRepository
                    .findBySparePartSparePartIdAndLocationLocationId(
                            item.getSparePart().getSparePartId(),
                            purchaseOrder.getLocation().getLocationId()
                    )
                    .orElseGet(() -> {
                        Stock created = new Stock();
                        created.setSparePart(item.getSparePart());
                        created.setLocation(purchaseOrder.getLocation());
                        created.setQuantity(0);
                        return created;
                    });

            stock.setQuantity(stock.getQuantity() + item.getQuantity());
            stockRepository.save(stock);
            item.setReceivedQuantity(item.getQuantity());
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        return poRepository.save(purchaseOrder);
    }
}
