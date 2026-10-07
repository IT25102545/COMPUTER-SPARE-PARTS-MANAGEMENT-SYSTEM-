package com.lankatech.spareparts.customer.service;
import com.lankatech.spareparts.common.entity.Location;
import com.lankatech.spareparts.common.repository.LocationRepository;
import com.lankatech.spareparts.inventory.repository.SparePartRepository;
import com.lankatech.spareparts.inventory.repository.StockRepository;
import com.lankatech.spareparts.inventory.entity.SparePart;
import com.lankatech.spareparts.inventory.entity.Stock;
import com.lankatech.spareparts.customer.dto.*;
import com.lankatech.spareparts.customer.entity.*;
import com.lankatech.spareparts.customer.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.time.LocalDateTime;

@Service @Transactional
public class CustomerService {
    private final CustomerRepository customers;
    private final ComplaintRepository complaints;
    private final ReservationRepository reservations;
    private final SparePartRepository parts;
    private final StockRepository stocks;
    private final LocationRepository locations;


    public CustomerService(CustomerRepository c,ComplaintRepository cp,ReservationRepository r,SparePartRepository p,StockRepository s,LocationRepository l){customers=c;complaints=cp;reservations=r;parts=p;stocks=s;locations=l;}
    @Transactional(readOnly = true)
    public List<Stock> getStocks(){return stocks.findAll();}
    public List<Customer> getCustomers(){return customers.findAll();}
    public Customer addCustomer(Customer c){c.setCustomerId(null);return customers.save(c);}
    public List<Complaint> getComplaints(){return complaints.findAll();}
    public List<Reservation> getReservations(){return reservations.findAll();}
    public Complaint addComplaint(ComplaintRequestDTO r){Customer c=customers.findById(r.getCustomerId()).orElseThrow(()->new
            IllegalArgumentException("Customer not found"));Complaint x=new
            Complaint();x.setCustomer(c);x.setSubject(r.getSubject());x.setDescription(r.getDescription());x.setStatus(ComplaintStatus
            .OPEN);return complaints.save(x);}
    public Complaint startComplaint(Long id) { return transitionComplaint(id, ComplaintStatus.OPEN, ComplaintStatus.IN_PROGRESS); }
    public Complaint resolve(Long id) { return transitionComplaint(id, ComplaintStatus.IN_PROGRESS, ComplaintStatus.RESOLVED); }
    public Complaint closeComplaint(Long id) { return transitionComplaint(id, ComplaintStatus.RESOLVED, ComplaintStatus.CLOSED); }
    private Complaint transitionComplaint(Long id, ComplaintStatus expected, ComplaintStatus next) {
        Complaint complaint = complaints.findForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Complaint not found"));
        if (complaint.getStatus() != expected)
            throw new IllegalStateException("Only " + expected + " complaints can change to " + next);
        complaint.setStatus(next);
        if (next == ComplaintStatus.RESOLVED) complaint.setResolvedAt(LocalDateTime.now());
        return complaints.save(complaint);
    }
    public Reservation reserve(ReservationRequestDTO r){Customer c=customers.findById(r.getCustomerId()).orElseThrow(()->new
            IllegalArgumentException("Customer not found"));SparePart p=parts.findById(r.getSparePartId()).orElseThrow(()->new
            IllegalArgumentException("Part not found"));Location l=locations.findById(r.getLocationId()).orElseThrow(()->new
            IllegalArgumentException("Location not found"));Stock
            s=stocks.findBySparePartSparePartIdAndLocationLocationId(r.getSparePartId(),r.getLocationId()).orElseThrow(()->new
            IllegalStateException("Stock not found"));if(s.getQuantity()<r.getQuantity())throw new IllegalStateException("Not enough stock to reserve");Reservation x=new
            Reservation();x.setCustomer(c);x.setSparePart(p);x.setLocation(l);x.setQuantity(r.getQuantity());x.setStatus("PENDING");return reservations.save(x);}
    public Reservation acceptReservation(Long id) { return transitionReservation(id, "PENDING", "ACTIVE"); }
    public Reservation rejectReservation(Long id) { return transitionReservation(id, "PENDING", "REJECTED"); }
    public Reservation completeReservation(Long id) { return transitionReservation(id, "ACTIVE", "COMPLETED"); }
    public Reservation cancelReservation(Long id) { return transitionReservation(id, "ACTIVE", "CANCELLED"); }

    private Reservation transitionReservation(Long id, String expected, String next) {
        Reservation reservation = reservations.findForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        if (!expected.equals(reservation.getStatus())) {
            throw new IllegalStateException("Only " + expected + " reservations can change to " + next);
        }
        if ("ACTIVE".equals(next) || "COMPLETED".equals(next)) deductStock(reservation);
        if ("CANCELLED".equals(next) && Boolean.TRUE.equals(reservation.getStockDeducted())) {
            Stock stock = lockedStock(reservation);
            stock.setQuantity(Math.addExact(stock.getQuantity(), reservation.getQuantity()));
            stocks.save(stock);
            reservation.setStockDeducted(false);
        }
        reservation.setStatus(next);
        return reservations.save(reservation);
    }

    public Reservation reconcileReservationStock(Long id) {
        Reservation reservation = reservations.findForUpdate(id)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
        if (!"ACTIVE".equals(reservation.getStatus()) && !"COMPLETED".equals(reservation.getStatus()))
            throw new IllegalStateException("Only active or completed reservations can be reconciled");
        deductStock(reservation);
        return reservations.save(reservation);
    }

    private Stock lockedStock(Reservation reservation) {
        if (reservation.getQuantity() == null || reservation.getQuantity() <= 0)
            throw new IllegalStateException("Reservation quantity must be positive");
        return stocks.findForUpdate(reservation.getSparePart().getSparePartId(), reservation.getLocation().getLocationId())
                .orElseThrow(() -> new IllegalStateException("Stock not found"));
    }

    private void deductStock(Reservation reservation) {
        if (Boolean.TRUE.equals(reservation.getStockDeducted())) return;
        Stock stock = lockedStock(reservation);
        if (stock.getQuantity() < reservation.getQuantity())
            throw new IllegalStateException("Not enough available stock to accept or reconcile this reservation");
        stock.setQuantity(stock.getQuantity() - reservation.getQuantity());
        stocks.save(stock);
        reservation.setStockDeducted(true);
    }
}
