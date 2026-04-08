package com.rra.taxhandbook.employee.service;

import com.rra.taxhandbook.employee.entity.EmployeeDirectorySnapshot;

public interface EmployeeVerificationService {

	EmployeeDirectorySnapshot verifyActiveEmployee(String employeeId, String email);
}
