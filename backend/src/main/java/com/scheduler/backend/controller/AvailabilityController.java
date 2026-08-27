package com.scheduler.backend.controller;

import com.scheduler.backend.dto.AvailabilityRequestDto;
import com.scheduler.backend.dto.AvailabilityResponseDto;
import com.scheduler.backend.service.AvailabilityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @PostMapping
    public ResponseEntity<AvailabilityResponseDto> createAvailability(@Valid @RequestBody AvailabilityRequestDto request) {
        AvailabilityResponseDto created = availabilityService.createAvailability(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping
    public ResponseEntity<List<AvailabilityResponseDto>> getAllAvailability(
            @RequestParam(required = false) UUID employeeId) {
        if (employeeId != null) {
            return ResponseEntity.ok(availabilityService.getAvailabilityByEmployee(employeeId));
        }
        return ResponseEntity.ok(availabilityService.getAllAvailability());
    }
}