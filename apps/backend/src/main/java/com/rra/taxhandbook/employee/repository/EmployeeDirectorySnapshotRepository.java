package com.rra.taxhandbook.employee.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.employee.entity.EmployeeDirectorySnapshot;

public interface EmployeeDirectorySnapshotRepository extends JpaRepository<EmployeeDirectorySnapshot, String> {

	Optional<EmployeeDirectorySnapshot> findByEmail(String email);
}
