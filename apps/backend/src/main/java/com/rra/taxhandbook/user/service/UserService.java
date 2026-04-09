package com.rra.taxhandbook.user.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.enums.UserRole;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.service.RoleService;
import com.rra.taxhandbook.user.dto.UserRequest;
import com.rra.taxhandbook.user.dto.UserResponse;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.entity.UserSource;
import com.rra.taxhandbook.user.repository.UserRepository;

@Service
public class UserService {

	private final UserRepository userRepository;
	private final RoleService roleService;

	public UserService(UserRepository userRepository, RoleService roleService) {
		this.userRepository = userRepository;
		this.roleService = roleService;
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
		if (request.fullName() == null || request.fullName().isBlank()) {
			throw new IllegalArgumentException("Full name is required.");
		}
		if (request.email() == null || request.email().isBlank()) {
			throw new IllegalArgumentException("Email is required.");
		}
		LanguageCode preferredLocale = request.preferredLocale() == null ? LanguageCode.EN : request.preferredLocale();
		Role role = roleService.getRoleByName(request.roleName());
		if (UserRole.PUBLIC.name().equalsIgnoreCase(role.getName())) {
			throw new IllegalArgumentException("PUBLIC cannot be assigned to a local authenticated system user.");
		}

		String userCode = generateUserCode(request.fullName());
		userRepository.findByEmail(request.email()).ifPresent(existing -> {
			throw new IllegalArgumentException("A system user already exists for email " + request.email());
		});
		userRepository.findByUserCode(userCode).ifPresent(existing -> {
			throw new IllegalArgumentException("A system user already exists for generated code " + userCode);
		});

		User user = new User(
			userCode,
			request.fullName().trim(),
			request.email().trim().toLowerCase(),
			preferredLocale,
			UserSource.LOCAL,
			"ACTIVE",
			Instant.now(),
			role
		);

		User savedUser = userRepository.save(user);
		return new ApiResponse<>("Local system user created successfully", toResponse(savedUser));
	}

	private UserResponse toResponse(User user) {
		return new UserResponse(
			user.getId(),
			user.getUserCode(),
			user.getFullName(),
			user.getEmail(),
			user.getRole().getName(),
			user.getPreferredLocale().name(),
			user.getSource().name(),
			user.getStatus()
		);
	}

	private String generateUserCode(String fullName) {
		String normalized = fullName == null ? "user" : fullName.trim().replaceAll("[^A-Za-z0-9]+", "-").replaceAll("(^-|-$)", "");
		if (normalized.isBlank()) {
			normalized = "user";
		}
		return "LOCAL-" + normalized.toUpperCase() + "-" + Instant.now().toEpochMilli();
	}
}
