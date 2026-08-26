package com.scheduler.backend.repository;

import com.scheduler.backend.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {

    Optional<Employee> findByEmail(String email);

    @Query("SELECT e FROM Employee e JOIN e.skills s WHERE s.id = :skillId")
    List<Employee> findBySkillId(@Param("skillId") UUID skillId);
}