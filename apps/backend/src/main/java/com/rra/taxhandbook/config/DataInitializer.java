package com.rra.taxhandbook.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.entity.UserSource;
import com.rra.taxhandbook.user.repository.UserRepository;

import java.time.Instant;

@Component
public class DataInitializer implements CommandLineRunner {

	private final RoleRepository roleRepository;
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(
		RoleRepository roleRepository,
		UserRepository userRepository,
		PasswordEncoder passwordEncoder
	) {
		this.roleRepository = roleRepository;
		this.userRepository = userRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) throws Exception {
		initializeRoles();
		initializeAdminUser();
	}

	private void initializeRoles() {
		seedRole("ADMIN", "Administrator role with full access");
		seedRole("CONTENT_OFFICER", "Content officer role");
		seedRole("VIEWER", "Viewer role with read-only access");
	}

	private void seedRole(String name, String description) {
		roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name, description)));
	}

	private void initializeAdminUser() {
		String adminUsername = "admin@rra.gov.rw";

		if (userRepository.findByEmail(adminUsername).isPresent()) {
			return;
		}

		Role adminRole = roleRepository.findByName("ADMIN")
			.orElseThrow(() -> new RuntimeException("ADMIN role not found"));

		String encodedPassword = passwordEncoder.encode("Admin@123");

		User adminUser = new User(
			"RRA-ADMIN",
			"System Administrator",
			adminUsername,
			encodedPassword,
			LanguageCode.EN,
			UserSource.LOCAL,
			"ACTIVE",
			null,
			null,
			null,
			null,
			Instant.now(),
			adminRole
		);

		userRepository.save(adminUser);
	}
}
