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
import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@Transactional
public class SupplierPurchaseService {

    private final SupplierRepository supplierRepository;
    private final PurchaseOrderRepository poRepository;
    private final LocationRepository locationRepository;
    private final UserRepository userRepository;
    private final SparePartRepository sparePartRepository;
    private final StockRepository stockRepository;

    private static final int MAX_NAME = 150;
    private static final int MAX_CONTACT = 100;
    private static final int MAX_PHONE = 12;
    private static final int MAX_EMAIL = 120;
    private static final int MAX_ADDRESS = 255;
    private static final int MAX_QUANTITY = 10000;
    private static final BigDecimal MAX_MONEY = new BigDecimal("9999999999.99");
    private static final Pattern NAME_PATTERN = Pattern.compile("^[A-Za-z0-9][A-Za-z0-9 .,&'()\\-]{1,149}$");
    private static final Pattern CONTACT_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z .'\\-]{1,99}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(?:07\\d{8}|0\\d{9}|\\+94\\d{9})$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

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
        String supplierName = cleanRequired(supplier.getSupplierName(), "Supplier name", MAX_NAME);
        if (!NAME_PATTERN.matcher(supplierName).matches()) {
            throw new IllegalArgumentException("Supplier name must be 2 to 150 characters and use letters, numbers, spaces, or . , & ' ( ) -");
        }

        String contactPerson = cleanOptional(supplier.getContactPerson(), "Contact person", MAX_CONTACT);
        if (contactPerson != null && !CONTACT_PATTERN.matcher(contactPerson).matches()) {
            throw new IllegalArgumentException("Contact person must be 2 to 100 letters and may include spaces, . ' -");
        }

        String phone = normalizePhone(supplier.getPhone());
        String email = cleanOptional(supplier.getEmail(), "Email", MAX_EMAIL);
        if (email != null) {
            email = email.toLowerCase();
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                throw new IllegalArgumentException("Enter a valid email address, up to 120 characters");
            }
        }

        String address = cleanOptional(supplier.getAddress(), "Address", MAX_ADDRESS);

        supplier.setSupplierId(null);
        supplier.setSupplierName(supplierName);
        supplier.setContactPerson(contactPerson);
        supplier.setPhone(phone);
        supplier.setEmail(email);
        supplier.setAddress(address);
        if (supplier.getActive() == null) {
            supplier.setActive(true);
        }
        return supplierRepository.save(supplier);
    }

    public List<PurchaseOrder> getOrders() {
        return poRepository.findAll();
    }

    public List<Location> getLocations() {
        return locationRepository.findAll();
    }

    public List<SparePart> getParts() {
        return sparePartRepository.findAll();
    }

    public PurchaseOrder createOrder(PurchaseOrderRequestDTO request) {
        Supplier supplier = supplierRepository.findById(request.getSupplierId())
                .orElseThrow(() -> new IllegalArgumentException("Supplier not found"));
        if (Boolean.FALSE.equals(supplier.getActive())) {
            throw new IllegalArgumentException("Inactive suppliers cannot be used for a new purchase order");
        }
        Location location = locationRepository.findById(request.getLocationId())
                .orElseThrow(() -> new IllegalArgumentException("Location not found"));
        User user = userRepository.findById(request.getCreatedById())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (Boolean.FALSE.equals(user.getActive())) {
            throw new IllegalArgumentException("Inactive user cannot create a purchase order");
        }

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setLocation(location);
        purchaseOrder.setCreatedBy(user);
        purchaseOrder.setStatus(PurchaseOrderStatus.ORDERED);

        BigDecimal total = BigDecimal.ZERO;
        Set<Long> sparePartIds = new HashSet<>();
        for (PurchaseOrderRequestDTO.Item itemRequest : request.getItems()) {
            if (!sparePartIds.add(itemRequest.getSparePartId())) {
                throw new IllegalArgumentException("The same spare part cannot be added twice in one purchase order");
            }
            if (itemRequest.getQuantity() == null || itemRequest.getQuantity() < 1 || itemRequest.getQuantity() > MAX_QUANTITY) {
                throw new IllegalArgumentException("Quantity must be a whole number from 1 to " + MAX_QUANTITY);
            }

            BigDecimal unitCost = normalizeMoney(itemRequest.getUnitCost(), "Unit cost");
            SparePart part = sparePartRepository.findById(itemRequest.getSparePartId())
                    .orElseThrow(() -> new IllegalArgumentException("Part not found"));
            if (Boolean.FALSE.equals(part.getActive())) {
                throw new IllegalArgumentException("Inactive spare parts cannot be ordered");
            }

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setPurchaseOrder(purchaseOrder);
            item.setSparePart(part);
            item.setQuantity(itemRequest.getQuantity());
            item.setUnitCost(unitCost);
            item.setReceivedQuantity(0);
            purchaseOrder.getItems().add(item);

            total = total.add(unitCost.multiply(BigDecimal.valueOf(itemRequest.getQuantity())));
        }

        total = normalizeMoney(total, "Order total");

        purchaseOrder.setTotalAmount(total);
        return poRepository.save(purchaseOrder);
    }

    public PurchaseOrder receive(Long id) {
        PurchaseOrder purchaseOrder = poRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PO not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new IllegalArgumentException("Only ordered PO can be received");
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

    public PurchaseOrder cancel(Long id) {
        PurchaseOrder purchaseOrder = poRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("PO not found"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.ORDERED) {
            throw new IllegalArgumentException("Only ordered PO can be cancelled");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        return poRepository.save(purchaseOrder);
    }

    private String cleanRequired(String value, String label, int maxLength) {
        String cleaned = cleanOptional(value, label, maxLength);
        if (cleaned == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        return cleaned;
    }

    private String cleanOptional(String value, String label, int maxLength) {
        if (value == null) {
            return null;
        }
        String cleaned = value.trim().replaceAll("\\s+", " ");
        if (cleaned.isEmpty()) {
            return null;
        }
        if (cleaned.length() > maxLength) {
            throw new IllegalArgumentException(label + " must be " + maxLength + " characters or fewer");
        }
        return cleaned;
    }

    private String normalizePhone(String value) {
        String phone = cleanOptional(value, "Phone", MAX_PHONE);
        if (phone == null) {
            return null;
        }
        phone = phone.replace(" ", "").replace("-", "");
        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new IllegalArgumentException("Phone must be a Sri Lankan number such as 0771234567 or +94771234567");
        }
        return phone;
    }

    private BigDecimal normalizeMoney(BigDecimal value, String label) {
        if (value == null) {
            throw new IllegalArgumentException(label + " is required");
        }
        if (value.signum() < 0) {
            throw new IllegalArgumentException(label + " cannot be negative");
        }
        try {
            value = value.setScale(2, RoundingMode.UNNECESSARY);
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(label + " can have at most 2 decimal places");
        }
        if (value.compareTo(MAX_MONEY) > 0) {
            throw new IllegalArgumentException(label + " is too large");
        }
        return value;
    }
}
