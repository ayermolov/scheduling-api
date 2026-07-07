package com.dispatch.schedulingapi.controller;

import com.dispatch.schedulingapi.dto.AppointmentRequestDTO;
import com.dispatch.schedulingapi.service.AppointmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
@CrossOrigin(origins = "*") // Allows your future React website to communicate with this API
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/schedule")
    public ResponseEntity<String> schedule(@RequestBody AppointmentRequestDTO request) {
        try {
            String result = appointmentService.scheduleAppointment(request);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }
}