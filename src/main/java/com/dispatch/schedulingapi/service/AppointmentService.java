package com.dispatch.schedulingapi.service;

import com.dispatch.schedulingapi.dto.AppointmentRequestDTO;
import com.dispatch.schedulingapi.model.Appointment;
import com.dispatch.schedulingapi.model.Customer;
import com.dispatch.schedulingapi.model.Technician;
import com.dispatch.schedulingapi.repository.AppointmentRepository;
import com.dispatch.schedulingapi.repository.CustomerRepository;
import com.dispatch.schedulingapi.repository.TechnicianRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final CustomerRepository customerRepository;
    private final TechnicianRepository technicianRepository;
    private final TwilioSmsService twilioSmsService;
    private final ZohoInvoiceService zohoInvoiceService;

    public AppointmentService(AppointmentRepository appointmentRepository,
                              CustomerRepository customerRepository,
                              TechnicianRepository technicianRepository,
                              TwilioSmsService twilioSmsService,
                              ZohoInvoiceService zohoInvoiceService) {
        this.appointmentRepository = appointmentRepository;
        this.customerRepository = customerRepository;
        this.technicianRepository = technicianRepository;
        this.twilioSmsService = twilioSmsService;
        this.zohoInvoiceService = zohoInvoiceService;
    }

    @Transactional
    public String scheduleAppointment(AppointmentRequestDTO request) {
        // 1. Fetch Customer and Technician from Database
        Customer customer = customerRepository.findById(request.customerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        Technician technician = technicianRepository.findById(request.technicianId())
                .orElseThrow(() -> new RuntimeException("Technician not found"));

        // 2. Save the Appointment to MySQL
        Appointment appointment = new Appointment();
        appointment.setCustomer(customer);
        appointment.setTechnician(technician);
        appointment.setAddress(request.address());
        appointment.setAppointmentTime(request.appointmentTime());
        appointment.setStatus("SCHEDULED");
        appointmentRepository.save(appointment);

        // 3. Send SMS to Technician
        String smsMessage = "New job at " + request.address() + " for " + customer.getName();
        twilioSmsService.sendSms(technician.getPhone(), smsMessage);

        // 4. Generate Zoho Draft Invoice (You will need to update your Zoho service to accept the price and serviceName)
        zohoInvoiceService.createDraftInvoice(customer.getId().toString(), request.serviceName(), request.price());

        return "Appointment scheduled successfully!";
    }
}