package com.lankatech.spareparts.sales.controller;

import com.lankatech.spareparts.sales.dto.SaleRequestDTO;
import com.lankatech.spareparts.sales.entity.Sale;
import com.lankatech.spareparts.sales.service.SalesService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SalesService salesService;

    public SalesController(SalesService salesService) {
        this.salesService = salesService;
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