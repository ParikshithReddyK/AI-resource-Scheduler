package com.scheduler.backend.repository;

import com.scheduler.backend.entity.Availability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AvailabilityRepository extends JpaRepository<Availability, UUID> {
    List<Availability> findByEmployeeId(UUID employeeId);
}