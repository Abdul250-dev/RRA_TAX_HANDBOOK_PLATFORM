package com.rra.taxhandbook.role.service;

import java.util.List;

import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.exception.ResourceNotFoundException;
import com.rra.taxhandbook.common.enums.UserRole;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;

@Service
public class RoleService {

	private final RoleRepository roleRepository;

	public RoleService(RoleRepository roleRepository) {
		this.roleRepository = roleRepository;
	}

	public List<Role> getRoles() {
		return roleRepository.findAll();
	}

	public Role getRoleByName(String name) {
		return roleRepository.findByName(name)
			.orElseThrow(() -> new ResourceNotFoundException("Role not found: " + name));
	}

	@org.springframework.context.annotation.Bean
	ApplicationRunner seedRoles() {
		return args -> {
			seedRole(UserRole.PUBLIC.name(), "Anonymous public access for reading published handbook content. This role is documented for access control but should not be assigned to authenticated users.");
			seedRole(UserRole.EDITOR.name(), "Content creation and draft editing rights for articles, documents, and FAQs.");
			seedRole(UserRole.REVIEWER.name(), "Quality control role for reviewing submissions, requesting changes, and approving content.");
			seedRole(UserRole.PUBLISHER.name(), "Final publishing authority for making approved content visible, scheduling releases, and archiving content.");
			seedRole(UserRole.ADMIN.name(), "Operational administration role for user management, role assignment, taxonomy management, and workflow overrides.");
			seedRole(UserRole.SUPER_ADMIN.name(), "Highest-trust role with full system configuration, security, and audit visibility privileges.");
			seedRole(UserRole.AUDITOR.name(), "Read-only oversight role for compliance reviews, audit logs, and change monitoring.");
		};
	}

	private void seedRole(String name, String description) {
		roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name, description)));
	}
}
