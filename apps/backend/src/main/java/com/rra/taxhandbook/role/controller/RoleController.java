package com.rra.taxhandbook.role.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.role.dto.RoleRequest;
import com.rra.taxhandbook.role.dto.RoleResponse;
import com.rra.taxhandbook.role.service.RoleService;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

	private final RoleService roleService;

	public RoleController(RoleService roleService) {
		this.roleService = roleService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public List<RoleResponse> getRoles() {
		return roleService.getRoles();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public RoleResponse getRole(@PathVariable Long id) {
		return roleService.getRole(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ApiResponse<RoleResponse> createRole(@RequestBody RoleRequest request) {
		return roleService.createRole(request);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ApiResponse<RoleResponse> updateRole(@PathVariable Long id, @RequestBody RoleRequest request) {
		return roleService.updateRole(id, request);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('SUPER_ADMIN')")
	public ApiResponse<String> deleteRole(@PathVariable Long id) {
		return roleService.deleteRole(id);
	}
}
