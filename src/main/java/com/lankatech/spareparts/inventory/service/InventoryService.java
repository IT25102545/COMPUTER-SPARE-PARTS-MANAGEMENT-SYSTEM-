package com.lankatech.spareparts.inventory.service;

import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.common.repository.LocationRepository;
import com.lankatech.spareparts.inventory.dto.*;
import com.lankatech.spareparts.inventory.entity.*;
import com.lankatech.spareparts.inventory.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class InventoryService {

    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final SparePartRepository sparePartRepository;
    private final StockRepository stockRepository;
    private final LocationRepository locationRepository;

    public InventoryService(
            CategoryRepository categoryRepository,
            BrandRepository brandRepository,
            SparePartRepository sparePartRepository,
            StockRepository stockRepository,
            LocationRepository locationRepository) {

        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.sparePartRepository = sparePartRepository;
        this.stockRepository = stockRepository;
        this.locationRepository = locationRepository;
    }

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public Category createCategory(Category c) {
        c.setCategoryId(null);
        return categoryRepository.save(c);
    }

    public List<Brand> getBrands() {
        return brandRepository.findAll();
    }

    public Brand createBrand(Brand b) {
        b.setBrandId(null);
        return brandRepository.save(b);
    }

    public List<SparePart> getParts() {
        return sparePartRepository.findAll();
    }

    public List<Stock> getStocks() {
        return stockRepository.findAll();
    }

    public SparePart createPart(SparePartRequestDTO r) {

        Category category = categoryRepository.findById(r.getCategoryId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Category not found"));

        Brand brand = brandRepository.findById(r.getBrandId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Brand not found"));

        if (sparePartRepository.findByPartCode(r.getPartCode()).isPresent()) {
            throw new IllegalArgumentException("Part code already exists");
        }

        SparePart p = new SparePart();

        p.setPartCode(r.getPartCode());
        p.setPartName(r.getPartName());
        p.setDescription(r.getDescription());
        p.setCategory(category);
        p.setBrand(brand);
        p.setUnitPrice(r.getUnitPrice());
        p.setReorderLevel(r.getReorderLevel());
        p.setActive(true);

        return sparePartRepository.save(p);
    }

    public Stock adjustStock(StockAdjustRequestDTO r) {

        SparePart part = sparePartRepository.findById(r.getSparePartId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Spare part not found"));

        Location location = locationRepository.findById(r.getLocationId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Location not found"));

        Stock stock = stockRepository
                .findBySparePartSparePartIdAndLocationLocationId(
                        r.getSparePartId(),
                        r.getLocationId()
                )
                .orElseGet(() -> {

                    Stock s = new Stock();

                    s.setSparePart(part);
                    s.setLocation(location);
                    s.setQuantity(0);

                    return s;
                });

        int newQty = stock.getQuantity() + r.getQuantityChange();

        if (newQty < 0) {
            throw new IllegalStateException(
                    "Stock cannot become negative"
            );
        }

        stock.setQuantity(newQty);

        return stockRepository.save(stock);
    }
}