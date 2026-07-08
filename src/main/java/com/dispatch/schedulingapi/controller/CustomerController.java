package com.dispatch.schedulingapi.controller;

import com.dispatch.schedulingapi.dto.CustomerRequestDTO;
import com.dispatch.schedulingapi.model.Customer;
import com.dispatch.schedulingapi.service.CustomerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/customers")
@CrossOrigin(origins = "*") // Crucial: This tells Spring Boot to allow traffic from your future React app
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    // Listens for POST requests at http://localhost:8080/api/customers
    @PostMapping
    public ResponseEntity<?> createCustomer(@RequestBody CustomerRequestDTO request) {
        try {
            // Hand the web data directly to the service we built earlier
            Customer newCustomer = customerService.createNewCustomer(
                    request.name(),
                    request.email(),
                    request.phone()
            );

            // Return a 200 OK status along with the saved customer data
            return ResponseEntity.ok(newCustomer);

        } catch (Exception e) {
            // If Zoho fails or the database crashes, return a 400 Bad Request
            return ResponseEntity.badRequest().body("Error creating customer: " + e.getMessage());
        }
    }
}