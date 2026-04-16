package com.rra.taxhandbook.role.service;

import java.util.List;
import java.util.Locale;

import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.audit.service.AuditLogService;
import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.common.enums.UserRole;
import com.rra.taxhandbook.role.dto.RoleRequest;
import com.rra.taxhandbook.role.dto.RoleResponse;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;
import com.rra.taxhandbook.user.repository.UserRepository;

@Service
public class RoleService {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final AuditLogService auditLogService;

	public RoleService(RoleRepository roleRepository, UserRepository userRepository, AuditLogService auditLogService) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.auditLogService = auditLogService;
	}

	public List<RoleResponse> getRoles() {
		return roleRepository.findAll().stream()
			.map(this::toResponse)
			.toList();
	}

	public RoleResponse getRole(Long id) {
		return roleRepository.findById(id)
			.map(this::toResponse)
			.orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
	}

	public Role getRoleByName(String name) {
		return roleRepository.findByName(name)
			.orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
	}

	public ApiResponse<RoleResponse> createRole(RoleRequest request, String actor) {
		validateRoleRequest(request);
		String normalizedName = normalizeName(request.name());
		roleRepository.findByName(normalizedName).ifPresent(existing -> {
			throw new IllegalArgumentException("Role already exists: " + normalizedName);
		});
		Role savedRole = roleRepository.save(new Role(normalizedName, request.description().trim()));
		auditLogService.log("ROLE_CREATED", actor, savedRole.getName(), "Role created with description: " + savedRole.getDescription());
		return new ApiResponse<>("Role created successfully", toResponse(savedRole));
	}

	public ApiResponse<RoleResponse> updateRole(Long id, RoleRequest request, String actor) {
		validateRoleRequest(request);
		Role role = roleRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
		if (isSystemRole(role.getName()) && !role.getName().equals(normalizeName(request.name()))) {
			throw new IllegalArgumentException("Built-in system role names cannot be renamed.");
		}

		String normalizedName = normalizeName(request.name());
		roleRepository.findByName(normalizedName).ifPresent(existing -> {
			if (!existing.getId().equals(id)) {
				throw new IllegalArgumentException("Role already exists: " + normalizedName);
			}
		});

		role.update(normalizedName, request.description().trim());
		Role savedRole = roleRepository.save(role);
		auditLogService.log("ROLE_UPDATED", actor, savedRole.getName(), "Role updated");
		return new ApiResponse<>("Role updated successfully", toResponse(savedRole));
	}

	public ApiResponse<String> deleteRole(Long id, String actor) {
		Role role = roleRepository.findById(id)
			.orElseThrow(() -> new ResourceNotFoundException("Role not found: " + id));
		if (isSystemRole(role.getName())) {
			throw new IllegalArgumentException("Built-in system roles cannot be deleted.");
		}
		if (userRepository.countByRole_Id(id) > 0) {
			throw new IllegalArgumentException("Role cannot be deleted while it is assigned to users.");
		}
		roleRepository.delete(role);
		auditLogService.log("ROLE_DELETED", actor, role.getName(), "Role deleted");
		return new ApiResponse<>("Role deleted successfully", role.getName());
	}

	@org.springframework.context.annotation.Bean
	@org.springframework.core.annotation.Order(0)
	ApplicationRunner seedRoles() {
		return args -> {
			seedRole(UserRole.PUBLIC.name(), "Anonymous public access for reading published handbook content. This role is documented for access control but should not be assigned to authenticated users.");
			seedRole(UserRole.EDITOR.name(), "Content creation and draft editing rights for articles, documents, and FAQs.");
			seedRole(UserRole.REVIEWER.name(), "Quality control role for reviewing submissions, requesting changes, and approving content.");
			seedRole(UserRole.PUBLISHER.name(), "Final publishing authority for making approved content visible, scheduling releases, and archiving content.");
			seedRole(UserRole.ADMIN.name(), "Highest privileged operational role for user management, role assignment, taxonomy management, workflow overrides, configuration, and audit oversight.");
			seedRole(UserRole.AUDITOR.name(), "Read-only oversight role for compliance reviews, audit logs, and change monitoring.");
		};
	}

	private void seedRole(String name, String description) {
		roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name, description)));
	}

	private RoleResponse toResponse(Role role) {
		return new RoleResponse(role.getId(), role.getName(), role.getDescription(), isSystemRole(role.getName()));
	}

	private boolean isSystemRole(String name) {
		return java.util.Arrays.stream(UserRole.values()).anyMatch(role -> role.name().equalsIgnoreCase(name));
	}

	private void validateRoleRequest(RoleRequest request) {
		if (request.name() == null || request.name().isBlank()) {
			throw new IllegalArgumentException("Role name is required.");
		}
		if (request.description() == null || request.description().isBlank()) {
			throw new IllegalArgumentException("Role description is required.");
		}
	}

	private String normalizeName(String name) {
		return name.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]+", "_");
	}
}
