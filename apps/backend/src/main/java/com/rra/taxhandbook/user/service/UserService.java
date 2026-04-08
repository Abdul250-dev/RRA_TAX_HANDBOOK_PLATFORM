package com.rra.taxhandbook.user.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.employee.entity.EmployeeDirectorySnapshot;
import com.rra.taxhandbook.employee.service.EmployeeVerificationService;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.service.RoleService;
import com.rra.taxhandbook.user.dto.UserRequest;
import com.rra.taxhandbook.user.dto.UserResponse;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final RoleService roleService;
	private final EmployeeVerificationService employeeVerificationService;

	public UserService(
		UserRepository userRepository,
		RoleService roleService,
		EmployeeVerificationService employeeVerificationService
	) {
		this.userRepository = userRepository;
		this.roleService = roleService;
		this.employeeVerificationService = employeeVerificationService;
	}

	public List<UserResponse> getUsers() {
		return userRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	public UserResponse getUserById(Long id) {
		return userRepository.findById(id)
			.map(this::toResponse)
			.orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
	}

	public ApiResponse<UserResponse> createUser(UserRequest request) {
		EmployeeDirectorySnapshot employee = employeeVerificationService.verifyActiveEmployee(request.employeeId(), request.email());
		Role role = roleService.getRoleByName(request.roleName());

		userRepository.findByEmployeeId(request.employeeId()).ifPresent(existing -> {
			throw new IllegalArgumentException("A system user already exists for employee " + request.employeeId());
		});

		userRepository.findByEmail(request.email()).ifPresent(existing -> {
			throw new IllegalArgumentException("A system user already exists for email " + request.email());
		});

		User user = new User(
			employee.getEmployeeId(),
			employee.getFullName(),
			employee.getEmail(),
			"ACTIVE",
			Instant.now(),
			role
		);

		User savedUser = userRepository.save(user);
		return new ApiResponse<>("User created from verified RRA employee directory entry", toResponse(savedUser));
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getEmployeeId(),
			user.getFullName(),
			user.getEmail(),
			user.getRole().getName()
		);
	}
}
