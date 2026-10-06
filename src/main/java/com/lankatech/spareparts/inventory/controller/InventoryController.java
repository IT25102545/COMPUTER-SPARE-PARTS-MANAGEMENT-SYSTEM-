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
    public InventoryController(InventoryService service) { this.service = service; }

    // ---- Categories ----
    @GetMapping("/categories")
    public List<Category> categories() { return service.getCategories(); }

    @PostMapping("/categories") @ResponseStatus(HttpStatus.CREATED)
    public Category createCategory(@RequestBody Category c) { return service.createCategory(c); }

    @PutMapping("/categories/{id}")
    public Category updateCategory(@PathVariable Long id, @RequestBody Category c) {
        return service.updateCategory(id, c);
    }

    @DeleteMapping("/categories/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) { service.deleteCategory(id); }

    // ---- Brands ----
    @GetMapping("/brands")
    public List<Brand> brands() { return service.getBrands(); }

    @PostMapping("/brands") @ResponseStatus(HttpStatus.CREATED)
    public Brand createBrand(@RequestBody Brand b) { return service.createBrand(b); }

    @PutMapping("/brands/{id}")
    public Brand updateBrand(@PathVariable Long id, @RequestBody Brand b) {
        return service.updateBrand(id, b);
    }

    @DeleteMapping("/brands/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBrand(@PathVariable Long id) { service.deleteBrand(id); }

    // ---- Spare parts ----
    @GetMapping("/parts")
    public List<SparePart> parts() { return service.getParts(); }

    @PostMapping("/parts") @ResponseStatus(HttpStatus.CREATED)
    public SparePart createPart(@Valid @RequestBody SparePartRequestDTO r) { return service.createPart(r); }

    @PutMapping("/parts/{id}")
    public SparePart updatePart(@PathVariable Long id, @Valid @RequestBody SparePartRequestDTO r) {
        return service.updatePart(id, r);
    }

    @DeleteMapping("/parts/{id}") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePart(@PathVariable Long id) { service.deletePart(id); }

    // ---- Stock ----
    @GetMapping("/stocks")
    public List<Stock> stocks() { return service.getStocks(); }

    @PostMapping("/stocks/adjust")
    public Stock adjust(@Valid @RequestBody StockAdjustRequestDTO r) { return service.adjustStock(r); }
}