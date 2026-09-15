package com.lankatech.spareparts.customer.controller;
import com.lankatech.spareparts.customer.dto.*; import com.lankatech.spareparts.customer.entity.*; import
        com.lankatech.spareparts.customer.service.CustomerService;
import jakarta.validation.Valid; import org.springframework.web.bind.annotation.*; import java.util.List;
@RestController @RequestMapping("/api/customer")
public class CustomerController {
    private final CustomerService s; public CustomerController(CustomerService s){this.s=s;}
    @GetMapping("/customers") public List<Customer> customers(){return s.getCustomers();}
    @PostMapping("/customers") public Customer add(@RequestBody Customer c){return s.addCustomer(c);}
    @GetMapping("/complaints") public List<Complaint> complaints(){return s.getComplaints();}
    @PostMapping("/complaints") public Complaint complaint(@Valid @RequestBody ComplaintRequestDTO r){return
            s.addComplaint(r);}
    @PutMapping("/complaints/{id}/resolve") public Complaint resolve(@PathVariable Long id){return s.resolve(id);}
    @GetMapping("/reservations") public List<Reservation> reservations(){return s.getReservations();}
    @PostMapping("/reservations") public Reservation reserve(@Valid @RequestBody ReservationRequestDTO r){return
            s.reserve(r);}
    @PutMapping("/reservations/{id}/cancel") public Reservation cancel(@PathVariable Long id){return
            s.cancelReservation(id);}
}
