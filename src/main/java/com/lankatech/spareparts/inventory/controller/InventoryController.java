package com.lankatech.spareparts.inventory.controller;

import com.lankatech.spareparts.inventory.dto.*;
import com.lankatech.spareparts.inventory.entity.*;
import com.lankatech.spareparts.inventory.service.InventoryService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    private final InventoryService service;

    public InventoryController(InventoryService service) {
        this.service = service;
    }

    @GetMapping("/categories")
    public List<Category> categories() {
        return service.getCategories();
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(
            @RequestBody Category c) {

        return service.createCategory(c);
    }

    @GetMapping("/brands")
    public List<Brand> brands() {
        return service.getBrands();
    }

    @PostMapping("/brands")
    @ResponseStatus(HttpStatus.CREATED)
    public Brand createBrand(
            @RequestBody Brand b) {

        return service.createBrand(b);
    }

    @GetMapping("/parts")
    public List<SparePart> parts() {
        return service.getParts();
    }

    @PostMapping("/parts")
    @ResponseStatus(HttpStatus.CREATED)
    public SparePart createPart(
            @Valid @RequestBody SparePartRequestDTO r) {

        return service.createPart(r);
    }

    @GetMapping("/stocks")
    public List<Stock> stocks() {
        return service.getStocks();
    }

    @PostMapping("/stocks/adjust")
    public Stock adjust(
            @Valid @RequestBody StockAdjustRequestDTO r) {

        return service.adjustStock(r);
    }
}