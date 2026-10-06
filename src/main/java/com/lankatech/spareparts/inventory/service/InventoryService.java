package com.lankatech.spareparts.inventory.service;

import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.common.repository.LocationRepository;
import com.lankatech.spareparts.inventory.dto.*;
import com.lankatech.spareparts.inventory.entity.*;
import com.lankatech.spareparts.inventory.repository.*;
import org.springframework.dao.DataIntegrityViolationException;
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

    public InventoryService(CategoryRepository categoryRepository, BrandRepository brandRepository,
                            SparePartRepository sparePartRepository, StockRepository stockRepository,
                            LocationRepository locationRepository) {
        this.categoryRepository = categoryRepository;
        this.brandRepository = brandRepository;
        this.sparePartRepository = sparePartRepository;
        this.stockRepository = stockRepository;
        this.locationRepository = locationRepository;
    }

    // ---------- Categories ----------
    public List<Category> getCategories() { return categoryRepository.findAll(); }

    public Category createCategory(Category c) {
        c.setCategoryId(null);
        return categoryRepository.save(c);
    }

    public Category updateCategory(Long id, Category c) {
        Category x = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        x.setCategoryName(c.getCategoryName());
        x.setDescription(c.getDescription());
        if (c.getActive() != null) x.setActive(c.getActive());
        return categoryRepository.save(x);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id))
            throw new IllegalArgumentException("Category not found");
        long used = sparePartRepository.countByCategoryCategoryId(id);
        if (used > 0)
            throw new IllegalStateException("Cannot delete: " + used
                    + " spare part(s) still use this category. Mark it Inactive instead.");
        categoryRepository.deleteById(id);
    }

    // ---------- Brands ----------
    public List<Brand> getBrands() { return brandRepository.findAll(); }

    public Brand createBrand(Brand b) {
        b.setBrandId(null);
        return brandRepository.save(b);
    }

    public Brand updateBrand(Long id, Brand b) {
        Brand x = brandRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        x.setBrandName(b.getBrandName());
        x.setDescription(b.getDescription());
        if (b.getActive() != null) x.setActive(b.getActive());
        return brandRepository.save(x);
    }

    public void deleteBrand(Long id) {
        if (!brandRepository.existsById(id))
            throw new IllegalArgumentException("Brand not found");
        long used = sparePartRepository.countByBrandBrandId(id);
        if (used > 0)
            throw new IllegalStateException("Cannot delete: " + used
                    + " spare part(s) still use this brand. Mark it Inactive instead.");
        brandRepository.deleteById(id);
    }

    // ---------- Spare parts ----------
    public List<SparePart> getParts() { return sparePartRepository.findAll(); }

    public SparePart createPart(SparePartRequestDTO r) {
        Category category = categoryRepository.findById(r.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Brand brand = brandRepository.findById(r.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));
        if (sparePartRepository.findByPartCode(r.getPartCode()).isPresent())
            throw new IllegalArgumentException("Part code already exists");

        SparePart p = new SparePart();
        p.setPartCode(r.getPartCode());
        p.setPartName(r.getPartName());
        p.setDescription(r.getDescription());
        p.setCategory(category);
        p.setBrand(brand);
        p.setUnitPrice(r.getUnitPrice());
        p.setReorderLevel(r.getReorderLevel());
        p.setActive(r.getActive() == null ? true : r.getActive());
        return sparePartRepository.save(p);
    }

    public SparePart updatePart(Long id, SparePartRequestDTO r) {
        SparePart p = sparePartRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Spare part not found"));
        Category category = categoryRepository.findById(r.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Brand brand = brandRepository.findById(r.getBrandId())
                .orElseThrow(() -> new IllegalArgumentException("Brand not found"));

        sparePartRepository.findByPartCode(r.getPartCode()).ifPresent(other -> {
            if (!other.getSparePartId().equals(id))
                throw new IllegalArgumentException("Part code already exists");
        });

        p.setPartCode(r.getPartCode());
        p.setPartName(r.getPartName());
        p.setDescription(r.getDescription());
        p.setCategory(category);
        p.setBrand(brand);
        p.setUnitPrice(r.getUnitPrice());
        p.setReorderLevel(r.getReorderLevel());
        if (r.getActive() != null) p.setActive(r.getActive());
        return sparePartRepository.save(p);
    }

    public void deletePart(Long id) {
        if (!sparePartRepository.existsById(id))
            throw new IllegalArgumentException("Spare part not found");

        List<Stock> stocks = stockRepository.findBySparePartSparePartId(id);
        int units = stocks.stream().mapToInt(Stock::getQuantity).sum();
        if (units > 0)
            throw new IllegalStateException("Cannot delete: this part still has " + units
                    + " units in stock. Adjust the stock to 0 first, or mark the part Inactive instead.");

        try {
            stockRepository.deleteAll(stocks);
            sparePartRepository.deleteById(id);
            sparePartRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new IllegalStateException("Cannot delete: this part is used in transfers, purchases, sales "
                    + "or reservations. Mark it Inactive instead.");
        }
    }

    // ---------- Stock ----------
    public List<Stock> getStocks() { return stockRepository.findAll(); }

    public Stock adjustStock(StockAdjustRequestDTO r) {
        SparePart part = sparePartRepository.findById(r.getSparePartId())
                .orElseThrow(() -> new IllegalArgumentException("Spare part not found"));
        Location location = locationRepository.findById(r.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));

        Stock stock = stockRepository.findBySparePartSparePartIdAndLocationLocationId(
                r.getSparePartId(), r.getLocationId()).orElseGet(() -> {
            Stock s = new Stock();
            s.setSparePart(part);
            s.setLocation(location);
            s.setQuantity(0);
            return s;
        });

        int newQty = stock.getQuantity() + r.getQuantityChange();
        if (newQty < 0) throw new IllegalStateException("Stock cannot become negative");
        stock.setQuantity(newQty);
        return stockRepository.save(stock);
    }
}