package com.rra.taxhandbook.role.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.role.entity.Role;

@Service
public class RoleService {

	public List<Role> getRoles() {
		return List.of(
			new Role(1L, "ADMIN", "Full platform access"),
			new Role(2L, "EDITOR", "Content authoring access"),
			new Role(3L, "VIEWER", "Read-only access")
		);
	}
}
