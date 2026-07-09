package com.dispatch.schedulingapi.controller;

import com.dispatch.schedulingapi.model.Technician;
import com.dispatch.schedulingapi.repository.TechnicianRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/technicians")
@CrossOrigin(origins = "*") // Allow React to talk to this endpoint
public class TechnicianController {

    private final TechnicianRepository technicianRepository;

    public TechnicianController(TechnicianRepository technicianRepository) {
        this.technicianRepository = technicianRepository;
    }

    // Listens for GET requests at http://localhost:8080/api/technicians
    @GetMapping
    public ResponseEntity<List<Technician>> getAllTechnicians() {
        // Fetches every technician from MySQL and returns them as a JSON list
        return ResponseEntity.ok(technicianRepository.findAll());
    }
}