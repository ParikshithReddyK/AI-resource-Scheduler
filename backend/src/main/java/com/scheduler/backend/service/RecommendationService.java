package com.scheduler.backend.service;

import com.scheduler.backend.dto.RankedCandidateResponseDto;
import com.scheduler.backend.dto.ml.MlCandidateFeatureDto;
import com.scheduler.backend.dto.ml.MlRankedCandidateDto;
import com.scheduler.backend.dto.ml.MlRecommendRequestDto;
import com.scheduler.backend.dto.ml.MlRecommendResponseDto;
import com.scheduler.backend.entity.Assignment;
import com.scheduler.backend.entity.Availability;
import com.scheduler.backend.entity.Employee;
import com.scheduler.backend.entity.Shift;
import com.scheduler.backend.entity.Skill;
import com.scheduler.backend.exception.ResourceNotFoundException;
import com.scheduler.backend.repository.AssignmentRepository;
import com.scheduler.backend.repository.AvailabilityRepository;
import com.scheduler.backend.repository.EmployeeRepository;
import com.scheduler.backend.repository.ShiftRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RecommendationService {

    private static final int NEVER_ASSIGNED_DAYS = 999;

    private final ShiftRepository shiftRepository;
    private final EmployeeRepository employeeRepository;
    private final AvailabilityRepository availabilityRepository;
    private final AssignmentRepository assignmentRepository;
    private final MlRecommendationClient mlRecommendationClient;

    @Transactional(readOnly = true)
    public List<RankedCandidateResponseDto> getRankedCandidatesForShift(UUID shiftId) {
        Shift shift = shiftRepository.findById(shiftId)
                .orElseThrow(() -> new ResourceNotFoundException("Shift not found: " + shiftId));

        Skill requiredSkill = shift.getRequiredSkill();
        DayOfWeek shiftDay = shift.getDate().getDayOfWeek();

        List<Employee> skilledEmployees = employeeRepository.findBySkillId(requiredSkill.getId());

        List<Employee> availableCandidates = skilledEmployees.stream()
                .filter(employee -> isAvailableForShift(employee, shiftDay, shift))
                .collect(Collectors.toList());

        if (availableCandidates.isEmpty()) {
            return List.of();
        }

        // Build workload/recency features per candidate
        Map<UUID, Integer> workloadByEmployee = new HashMap<>();
        Map<UUID, Integer> daysSinceLastByEmployee = new HashMap<>();

        for (Employee employee : availableCandidates) {
            List<Assignment> history = assignmentRepository.findByEmployeeId(employee.getId());
            workloadByEmployee.put(employee.getId(), history.size());
            daysSinceLastByEmployee.put(employee.getId(), computeDaysSinceLastAssignment(history));
        }

        // Call the ML service to score and explain each candidate
        List<MlCandidateFeatureDto> features = availableCandidates.stream()
                .map(e -> MlCandidateFeatureDto.builder()
                        .employeeId(e.getId().toString())
                        .workloadCount(workloadByEmployee.get(e.getId()))
                        .daysSinceLastAssignment(daysSinceLastByEmployee.get(e.getId()))
                        .build())
                .collect(Collectors.toList());

        MlRecommendResponseDto mlResponse = mlRecommendationClient.rank(
                MlRecommendRequestDto.builder()
                        .shiftId(shiftId.toString())
                        .candidates(features)
                        .build()
        );

        Map<String, MlRankedCandidateDto> scoresByEmployeeId = mlResponse.getRanked().stream()
                .collect(Collectors.toMap(MlRankedCandidateDto::getEmployeeId, r -> r));

        return availableCandidates.stream()
                .map(employee -> {
                    MlRankedCandidateDto scored = scoresByEmployeeId.get(employee.getId().toString());
                    return RankedCandidateResponseDto.builder()
                            .employeeId(employee.getId())
                            .fullName(employee.getFullName())
                            .department(employee.getDepartment())
                            .skillNames(employee.getSkills().stream().map(Skill::getName).collect(Collectors.toSet()))
                            .workloadCount(workloadByEmployee.get(employee.getId()))
                            .daysSinceLastAssignment(daysSinceLastByEmployee.get(employee.getId()))
                            .score(scored != null ? scored.getScore() : 0.0)
                            .explanation(scored != null ? scored.getExplanation() : Map.of())
                            .build();
                })
                .sorted(Comparator.comparingDouble(RankedCandidateResponseDto::getScore).reversed())
                .collect(Collectors.toList());
    }

    private boolean isAvailableForShift(Employee employee, DayOfWeek shiftDay, Shift shift) {
        List<Availability> windows = availabilityRepository.findByEmployeeIdAndDayOfWeek(employee.getId(), shiftDay);
        return windows.stream().anyMatch(window ->
                !window.getStartTime().isAfter(shift.getStartTime()) &&
                        !window.getEndTime().isBefore(shift.getEndTime())
        );
    }

    private int computeDaysSinceLastAssignment(List<Assignment> history) {
        return history.stream()
                .map(Assignment::getAssignedAt)
                .max(Instant::compareTo)
                .map(last -> (int) ChronoUnit.DAYS.between(last, Instant.now()))
                .orElse(NEVER_ASSIGNED_DAYS);
    }
}