package com.scheduler.backend.service;

import com.scheduler.backend.dto.EmployeeRequestDto;
import com.scheduler.backend.dto.EmployeeResponseDto;
import com.scheduler.backend.entity.Employee;
import com.scheduler.backend.entity.Skill;
import com.scheduler.backend.exception.ResourceNotFoundException;
import com.scheduler.backend.repository.EmployeeRepository;
import com.scheduler.backend.repository.SkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final SkillRepository skillRepository;

    @Transactional
    public EmployeeResponseDto createEmployee(EmployeeRequestDto request) {
        Set<Skill> skills = new HashSet<>();
        if (request.getSkillIds() != null) {
            for (UUID skillId : request.getSkillIds()) {
                Skill skill = skillRepository.findById(skillId)
                        .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));
                skills.add(skill);
            }
        }

        Employee employee = Employee.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .department(request.getDepartment())
                .skills(skills)
                .build();

        Employee saved = employeeRepository.save(employee);
        return toResponseDto(saved);
    }

    @Transactional(readOnly = true)
    public EmployeeResponseDto getEmployeeById(UUID id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Employee not found: " + id));
        return toResponseDto(employee);
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponseDto> getAllEmployees() {
        return employeeRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    private EmployeeResponseDto toResponseDto(Employee employee) {
        Set<String> skillNames = employee.getSkills().stream()
                .map(Skill::getName)
                .collect(Collectors.toSet());

        return EmployeeResponseDto.builder()
                .id(employee.getId())
                .fullName(employee.getFullName())
                .email(employee.getEmail())
                .department(employee.getDepartment())
                .skillNames(skillNames)
                .createdAt(employee.getCreatedAt())
                .build();
    }
}