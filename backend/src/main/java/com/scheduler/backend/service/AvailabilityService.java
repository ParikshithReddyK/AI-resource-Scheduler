package com.scheduler.backend.service;

import com.scheduler.backend.dto.AvailabilityRequestDto;
import com.scheduler.backend.dto.AvailabilityResponseDto;
import com.scheduler.backend.entity.Availability;
import com.scheduler.backend.entity.Employee;
import com.scheduler.backend.exception.ResourceNotFoundException;
import com.scheduler.backend.repository.AvailabilityRepository;
import com.scheduler.backend.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AvailabilityService {

    private final AvailabilityRepository availabilityRepository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public AvailabilityResponseDto createAvailability(AvailabilityRequestDto request) {
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + request.getEmployeeId()));

        Availability availability = Availability.builder()
                .employee(employee)
                .dayOfWeek(request.getDayOfWeek())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .build();

        Availability saved = availabilityRepository.save(availability);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponseDto> getAvailabilityByEmployee(UUID employeeId) {
        return availabilityRepository.findByEmployeeId(employeeId).stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AvailabilityResponseDto> getAllAvailability() {
        return availabilityRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private AvailabilityResponseDto toResponseDto(Availability availability) {
        return AvailabilityResponseDto.builder()
                .id(availability.getId())
                .employeeId(availability.getEmployee().getId())
                .employeeName(availability.getEmployee().getFullName())
                .dayOfWeek(availability.getDayOfWeek())
                .startTime(availability.getStartTime())
                .endTime(availability.getEndTime())
                .build();
    }
}