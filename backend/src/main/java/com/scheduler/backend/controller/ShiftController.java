package com.scheduler.backend.controller;

import com.scheduler.backend.dto.ShiftRequestDto;
import com.scheduler.backend.dto.ShiftResponseDto;
import com.scheduler.backend.entity.ShiftStatus;
import com.scheduler.backend.service.ShiftService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/shifts")
@RequiredArgsConstructor
public class ShiftController {

    private final ShiftService shiftService;

    @PostMapping
    public ResponseEntity<ShiftResponseDto> createShift(@Valid @RequestBody ShiftRequestDto request) {
        ShiftResponseDto created = shiftService.createShift(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponseDto> getShift(@PathVariable UUID id) {
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }

    @GetMapping
    public ResponseEntity<List<ShiftResponseDto>> getAllShifts(
            @RequestParam(required = false) ShiftStatus status) {
        if (status != null) {
            return ResponseEntity.ok(shiftService.getShiftsByStatus(status));
        }
        return ResponseEntity.ok(shiftService.getAllShifts());
    }
}