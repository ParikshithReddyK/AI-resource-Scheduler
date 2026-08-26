package com.scheduler.backend.repository;

import com.scheduler.backend.entity.Shift;
import com.scheduler.backend.entity.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {
    List<Shift> findByStatus(ShiftStatus status);
}