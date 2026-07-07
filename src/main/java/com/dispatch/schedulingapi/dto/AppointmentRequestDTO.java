package com.dispatch.schedulingapi.dto;

import java.time.LocalDateTime;

public record AppointmentRequestDTO(
        Long customerId,
        Long technicianId,
        String address,
        LocalDateTime appointmentTime,
        String serviceName, // For the Zoho Invoice
        double price        // For the Zoho Invoice
) {
}
