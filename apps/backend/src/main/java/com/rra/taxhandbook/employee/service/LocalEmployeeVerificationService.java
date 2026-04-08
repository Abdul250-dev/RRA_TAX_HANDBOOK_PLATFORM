package com.rra.taxhandbook.employee.service;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.exception.UnauthorizedException;
import com.rra.taxhandbook.employee.entity.EmployeeDirectorySnapshot;
import com.rra.taxhandbook.employee.repository.EmployeeDirectorySnapshotRepository;

@Service
public class LocalEmployeeVerificationService implements EmployeeVerificationService {

	private final EmployeeDirectorySnapshotRepository employeeRepository;

	public LocalEmployeeVerificationService(EmployeeDirectorySnapshotRepository employeeRepository) {
		this.employeeRepository = employeeRepository;
	}

	@Override
	public EmployeeDirectorySnapshot verifyActiveEmployee(String employeeId, String email) {
		EmployeeDirectorySnapshot employee = employeeRepository.findById(employeeId)
			.orElseThrow(() -> new UnauthorizedException("Employee not found in the RRA directory snapshot."));

		if (!employee.isActive()) {
			throw new UnauthorizedException("Employee is not active in the RRA directory snapshot.");
		}

		if (!employee.getEmail().equalsIgnoreCase(email)) {
			throw new UnauthorizedException("Employee email does not match the RRA directory snapshot.");
		}

		return employee;
	}
}
