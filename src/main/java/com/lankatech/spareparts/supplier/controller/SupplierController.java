package com.lankatech.spareparts.supplier.controller;

import com.lankatech.spareparts.supplier.dto.PurchaseOrderRequestDTO;
import com.lankatech.spareparts.supplier.entity.PurchaseOrder;
import com.lankatech.spareparts.supplier.entity.Supplier;
import com.lankatech.spareparts.supplier.service.SupplierPurchaseService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/supplier")
public class SupplierController {

    private final SupplierPurchaseService service;

    public SupplierController(SupplierPurchaseService service) {
        this.service = service;
    }

    @GetMapping("/suppliers")
    public List<Supplier> suppliers() {
        return service.getSuppliers();
    }

    @PostMapping("/suppliers")
    @ResponseStatus(HttpStatus.CREATED)
    public Supplier add(@RequestBody Supplier supplier) {
        return service.createSupplier(supplier);
    }

    @GetMapping("/purchase-orders")
    public List<PurchaseOrder> orders() {
        return service.getOrders();
    }

    @PostMapping("/purchase-orders")
    @ResponseStatus(HttpStatus.CREATED)
    public PurchaseOrder create(@Valid @RequestBody PurchaseOrderRequestDTO request) {
        return service.createOrder(request);
    }

    @PutMapping("/purchase-orders/{id}/receive")
    public PurchaseOrder receive(@PathVariable Long id) {
        return service.receive(id);
    }
}
