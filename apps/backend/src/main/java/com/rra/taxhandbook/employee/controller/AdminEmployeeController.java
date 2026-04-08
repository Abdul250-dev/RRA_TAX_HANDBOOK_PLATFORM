package com.rra.taxhandbook.employee.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.employee.dto.EmployeeDirectoryResponse;
import com.rra.taxhandbook.employee.service.EmployeeDirectoryService;

@RestController
@RequestMapping("/api/admin/employees")
public class AdminEmployeeController {

	private final EmployeeDirectoryService employeeDirectoryService;

	public AdminEmployeeController(EmployeeDirectoryService employeeDirectoryService) {
		this.employeeDirectoryService = employeeDirectoryService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN','AUDITOR')")
	public List<EmployeeDirectoryResponse> getEmployees() {
		return employeeDirectoryService.getEmployees();
	}
}
