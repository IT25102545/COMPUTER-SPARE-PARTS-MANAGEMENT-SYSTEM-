package com.lankatech.spareparts.customer.service;
import com.lankatech.spareparts.common.entity.Location; import
        com.lankatech.spareparts.common.repository.LocationRepository;
import com.lankatech.spareparts.customer.dto.*; import com.lankatech.spareparts.customer.entity.*; import
        com.lankatech.spareparts.customer.repository.*;
import com.lankatech.spareparts.inventory.entity.*; import com.lankatech.spareparts.inventory.repository.*;
import org.springframework.stereotype.Service; import org.springframework.transaction.annotation.Transactional; import
        java.time.LocalDateTime; import java.util.List;
@Service @Transactional
public class CustomerService {
    private final CustomerRepository customers; private final ComplaintRepository complaints; private final
    ReservationRepository reservations;
    private final SparePartRepository parts; private final StockRepository stocks; private final LocationRepository
            locations;
    public CustomerService(CustomerRepository c,ComplaintRepository cp,ReservationRepository r,SparePartRepository
            p,StockRepository s,LocationRepository l){customers=c;complaints=cp;reservations=r;parts=p;stocks=s;locations=l;}
    public List<Customer> getCustomers(){return customers.findAll();} public Customer addCustomer(Customer
                                                                                                          c){c.setCustomerId(null);return customers.save(c);} public List<Complaint> getComplaints(){return complaints.findAll();}
    public List<Reservation> getReservations(){return reservations.findAll();}
    public Complaint addComplaint(ComplaintRequestDTO r){Customer c=customers.findById(r.getCustomerId()).orElseThrow(()->new
            IllegalArgumentException("Customer not found"));Complaint x=new
            Complaint();x.setCustomer(c);x.setSubject(r.getSubject());x.setDescription(r.getDescription());x.setStatus(ComplaintStatus
            .OPEN);return complaints.save(x);}
    public Complaint resolve(Long id){Complaint c=complaints.findById(id).orElseThrow(()->new
            IllegalArgumentException("Complaint not
            found"));c.setStatus(ComplaintStatus.RESOLVED);c.setResolvedAt(LocalDateTime.now());return complaints.save(c);}
        public Reservation reserve(ReservationRequestDTO r){Customer c=customers.findById(r.getCustomerId()).orElseThrow(()->new
                IllegalArgumentException("Customer not found"));SparePart p=parts.findById(r.getSparePartId()).orElseThrow(()->new
                IllegalArgumentException("Part not found"));Location l=locations.findById(r.getLocationId()).orElseThrow(()->new
                IllegalArgumentException("Location not found"));Stock
                s=stocks.findBySparePartSparePartIdAndLocationLocationId(r.getSparePartId(),r.getLocationId()).orElseThrow(()->new
                IllegalStateException("Stock not found"));if(s.getQuantity()<r.getQuantity())throw new IllegalStateException("Not enough
                stock to reserve");Reservation x=new
                Reservation();x.setCustomer(c);x.setSparePart(p);x.setLocation(l);x.setQuantity(r.getQuantity());x.setStatus("ACTIVE");ret
            urn reservations.save(x);}
        public Reservation cancelReservation(Long id){Reservation r=reservations.findById(id).orElseThrow(()->new
                IllegalArgumentException("Reservation not found"));r.setStatus("CANCELLED");return reservations.save(r);}
    }