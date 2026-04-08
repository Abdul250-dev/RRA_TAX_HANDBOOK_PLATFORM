package com.rra.taxhandbook.employee.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.employee.dto.EmployeeDirectoryResponse;
import com.rra.taxhandbook.employee.repository.EmployeeDirectorySnapshotRepository;

@Service
public class EmployeeDirectoryService {

	private final EmployeeDirectorySnapshotRepository employeeRepository;

	public EmployeeDirectoryService(EmployeeDirectorySnapshotRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	public List<EmployeeDirectoryResponse> getEmployees() {
		return employeeRepository.findAll().stream()
			.map(employee -> new EmployeeDirectoryResponse(
				employee.getEmployeeId(),
				employee.getFullName(),
				employee.getEmail(),
				employee.isActive()
			))
			.toList();
	}
}
