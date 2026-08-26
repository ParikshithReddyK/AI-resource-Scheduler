package com.scheduler.backend.repository;

import com.scheduler.backend.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AssignmentRepository extends JpaRepository<Assignment, UUID> {
    List<Assignment> findByEmployeeId(UUID employeeId);
    List<Assignment> findByShiftId(UUID shiftId);
}