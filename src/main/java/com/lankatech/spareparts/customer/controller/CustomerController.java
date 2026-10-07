package com.lankatech.spareparts.customer.controller;
import com.lankatech.spareparts.customer.dto.*; import com.lankatech.spareparts.customer.entity.*; import
        com.lankatech.spareparts.customer.service.CustomerService;
import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/customer")
public class CustomerController {
    private final CustomerService s; public CustomerController(CustomerService s){this.s=s;}
    @GetMapping("/stocks") public List<com.lankatech.spareparts.inventory.entity.Stock> stocks(){return s.getStocks();}
    @GetMapping("/customers") public List<Customer> customers(){return s.getCustomers();}
    @PostMapping("/customers") public Customer add(@Valid @RequestBody Customer c){return s.addCustomer(c);}
    @GetMapping("/complaints") public List<Complaint> complaints(){return s.getComplaints();}
    @PostMapping("/complaints") public Complaint complaint(@Valid @RequestBody ComplaintRequestDTO r){return
            s.addComplaint(r);}
    @PutMapping("/complaints/{id}/start") public Complaint startComplaint(@PathVariable Long id){return s.startComplaint(id);}
    @PutMapping("/complaints/{id}/close") public Complaint closeComplaint(@PathVariable Long id){return s.closeComplaint(id);}
    @PutMapping("/complaints/{id}/resolve") public Complaint resolve(@PathVariable Long id){return s.resolve(id);}
    @GetMapping("/reservations") public List<Reservation> reservations(){return s.getReservations();}
    @PostMapping("/reservations") public Reservation reserve(@Valid @RequestBody ReservationRequestDTO r){return
            s.reserve(r);}
    @PutMapping("/reservations/{id}/reconcile-stock") public Reservation reconcileStock(@PathVariable Long id){return s.reconcileReservationStock(id);}
    @PutMapping("/reservations/{id}/accept") public Reservation accept(@PathVariable Long id){return s.acceptReservation(id);}
    @PutMapping("/reservations/{id}/reject") public Reservation reject(@PathVariable Long id){return s.rejectReservation(id);}
    @PutMapping("/reservations/{id}/complete") public Reservation complete(@PathVariable Long id){return s.completeReservation(id);}
    @PutMapping("/reservations/{id}/cancel") public Reservation cancel(@PathVariable Long id){return
            s.cancelReservation(id);}
}