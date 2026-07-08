package com.dispatch.schedulingapi.service;

import com.dispatch.schedulingapi.model.Customer;
import com.dispatch.schedulingapi.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final ZohoInvoiceService zohoInvoiceService;

    public CustomerService(CustomerRepository customerRepository, ZohoInvoiceService zohoInvoiceService) {
        this.customerRepository = customerRepository;
        this.zohoInvoiceService = zohoInvoiceService;
    }

    @Transactional
    public Customer createNewCustomer(String name, String email, String phone) {
        // 1. Call Zoho to create the contact and get the 19-digit ID
        String newZohoId = zohoInvoiceService.createContact(name, email, phone);

        // 2. Create the local Customer object
        Customer customer = new Customer();
        customer.setName(name);
        customer.setEmail(email);
        customer.setPhone(phone);
        customer.setZohoContactId(newZohoId); // Link the two databases!

        // 3. Save to MySQL
        return customerRepository.save(customer);
    }
}