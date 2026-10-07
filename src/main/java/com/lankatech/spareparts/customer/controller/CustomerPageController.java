package com.lankatech.spareparts.customer.controller;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
@Controller
public class CustomerPageController {
    @GetMapping({"/customer-service", "/customer/", "/customer/index.html"})
    public String customerPage() { return "redirect:/customer.html"; }
}
