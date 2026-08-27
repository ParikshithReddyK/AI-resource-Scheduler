package com.scheduler.backend.service;

import com.scheduler.backend.dto.CandidateResponseDto;
import com.scheduler.backend.entity.Availability;
import com.scheduler.backend.entity.Employee;
import com.scheduler.backend.entity.Shift;
import com.scheduler.backend.entity.Skill;
import com.scheduler.backend.exception.ResourceNotFoundException;
import com.scheduler.backend.repository.AvailabilityRepository;
import com.scheduler.backend.repository.EmployeeRepository;
import com.scheduler.backend.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final AvailabilityRepository availabilityRepository;

    @Transactional(readOnly = true)
    public List<CandidateResponseDto> getCandidatesForShift(UUID shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + shiftId));

        Skill requiredSkill = shift.getRequiredSkill();
        DayOfWeek shiftDay = shift.getDate().getDayOfWeek();

        List<Employee> skilledEmployees = employeeRepository.findBySkillId(requiredSkill.getId());

        return skilledEmployees.stream()
                .filter(employee -> isAvailableForShift(employee, shiftDay, shift))
                .map(this::toCandidateDto)
                .collect(Collectors.toList());
    }

    private boolean isAvailableForShift(Employee employee, DayOfWeek shiftDay, Shift shift) {
        List<Availability> windows = availabilityRepository.findByEmployeeIdAndDayOfWeek(employee.getId(), shiftDay);

        return windows.stream().anyMatch(window ->
                !window.getStartTime().isAfter(shift.getStartTime()) &&
                        !window.getEndTime().isBefore(shift.getEndTime())
        );
    }

    private CandidateResponseDto toCandidateDto(Employee employee) {
        return CandidateResponseDto.builder()
                .employeeId(employee.getId())
                .fullName(employee.getFullName())
                .department(employee.getDepartment())
                .skillNames(employee.getSkills().stream()
                        .map(Skill::getName)
                        .collect(Collectors.toSet()))
                .build();
    }
}