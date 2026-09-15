package com.lankatech.spareparts.sales.service;

import com.lankatech.spareparts.auth.entity.User;
import com.lankatech.spareparts.auth.repository.UserRepository;
import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.common.repository.LocationRepository;
import com.lankatech.spareparts.customer.entity.Customer;
import com.lankatech.spareparts.customer.repository.CustomerRepository;
import com.lankatech.spareparts.inventory.entity.SparePart;
import com.lankatech.spareparts.inventory.entity.Stock;
import com.lankatech.spareparts.inventory.repository.SparePartRepository;
import com.lankatech.spareparts.inventory.repository.StockRepository;
import com.lankatech.spareparts.sales.dto.SaleRequestDTO;
import com.lankatech.spareparts.sales.entity.*;
import com.lankatech.spareparts.sales.repository.InvoiceRepository;
import com.lankatech.spareparts.sales.repository.PaymentRepository;
import com.lankatech.spareparts.sales.repository.SaleRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class SalesService {

    private final SaleRepository sales;
    private final PaymentRepository payments;
    private final InvoiceRepository invoices;

    private final SparePartRepository parts;
    private final StockRepository stocks;

    private final LocationRepository locations;
    private final UserRepository users;
    private final CustomerRepository customers;

    public SalesService(
            SaleRepository sales,
            PaymentRepository payments,
            InvoiceRepository invoices,
            SparePartRepository parts,
            StockRepository stocks,
            LocationRepository locations,
            UserRepository users,
            CustomerRepository customers
    ) {
        this.sales = sales;
        this.payments = payments;
        this.invoices = invoices;
        this.parts = parts;
        this.stocks = stocks;
        this.locations = locations;
        this.users = users;
        this.customers = customers;
    }

    public List<Sale> getSales() {
        return sales.findAll();
    }

    public Sale createSale(SaleRequestDTO r) {

        Location location = locations.findById(r.getLocationId())
                .orElseThrow(() ->
                        new IllegalArgumentException("Location not found"));

        User user = users.findById(r.getCashierUserId())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found"));

        Customer customer =
                r.getCustomerId() == null
                        ? null
                        : customers.findById(r.getCustomerId())
                          .orElseThrow(() ->
                                       new IllegalArgumentException("Customer not found"));

        Sale sale = new Sale();

        sale.setLocation(location);
        sale.setCashierUser(user);
        sale.setCustomer(customer);
        sale.setStatus(SaleStatus.COMPLETED);

        BigDecimal total = BigDecimal.ZERO;

        for (SaleRequestDTO.Item itemRequest : r.getItems()) {

            SparePart part = parts.findById(itemRequest.getSparePartId())
                    .orElseThrow(() ->
                            new IllegalArgumentException("Part not found"));

            Stock stock =
                    stocks.findBySparePartSparePartIdAndLocationLocationId(
                                    itemRequest.getSparePartId(),
                                    r.getLocationId()
                            )
                            .orElseThrow(() ->
                                    new IllegalStateException("Stock not found"));

            if (stock.getQuantity() < itemRequest.getQuantity()) {
                throw new IllegalStateException(
                        "Insufficient stock for " + part.getPartName()
                );
            }

            BigDecimal lineTotal =
                    part.getUnitPrice()
                            .multiply(
                                    BigDecimal.valueOf(
                                            itemRequest.getQuantity()
                                    )
                            );

            SaleItem saleItem = new SaleItem();

            saleItem.setSale(sale);
            saleItem.setSparePart(part);
            saleItem.setQuantity(itemRequest.getQuantity());
            saleItem.setUnitPrice(part.getUnitPrice());
            saleItem.setLineTotal(lineTotal);

            sale.getItems().add(saleItem);

            total = total.add(lineTotal);

            stock.setQuantity(
                    stock.getQuantity() - itemRequest.getQuantity()
            );

            stocks.save(stock);
        }

        sale.setTotalAmount(total);

        Sale savedSale = sales.save(sale);

        Payment payment = new Payment();
        payment.setSale(savedSale);
        payment.setAmount(total);
        payment.setPaymentMethod(r.getPaymentMethod());

        payments.save(payment);

        Invoice invoice = new Invoice();
        invoice.setSale(savedSale);
        invoice.setInvoiceNumber(
                "INV-" + System.currentTimeMillis()
        );

        invoices.save(invoice);

        return savedSale;
    }
}