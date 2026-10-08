package com.lankatech.spareparts.sales.controller;

import com.lankatech.spareparts.sales.dto.SaleRequestDTO;
import com.lankatech.spareparts.sales.entity.Sale;
import com.lankatech.spareparts.sales.service.SalesService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;import java.util.Map;

@RestController
@RequestMapping("/api/sales")


public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    @GetMapping("/available-stock")
    public Map<String, Object> getAvailableStock(
            @RequestParam Long sparePartId,
            @RequestParam Long locationId) {

        int quantity = salesService.getAvailableStock(sparePartId, locationId);

        return Map.of(
                "sparePartId", sparePartId,
                "locationId", locationId,
                "availableStock", quantity
        );
    }

    @GetMapping
    public List<Sale> list() {
        return salesService.getSales();
    }

    @PostMapping
    public Sale create(@Valid @RequestBody SaleRequestDTO request) {
        return salesService.createSale(request);
    }
}