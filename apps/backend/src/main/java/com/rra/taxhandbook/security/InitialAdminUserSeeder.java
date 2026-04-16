package com.rra.taxhandbook.security;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.rra.taxhandbook.common.enums.LanguageCode;
import com.rra.taxhandbook.common.enums.UserRole;
import com.rra.taxhandbook.role.entity.Role;
import com.rra.taxhandbook.role.repository.RoleRepository;
import com.rra.taxhandbook.user.entity.User;
import com.rra.taxhandbook.user.entity.UserSource;
import com.rra.taxhandbook.user.repository.UserRepository;

@Component
@Order(10)
public class InitialAdminUserSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(InitialAdminUserSeeder.class);

	@Value("${app.security.initial-admin.enabled:false}")
	private boolean initialAdminEnabled;

	@Value("${app.security.initial-admin.employee-id:}")
	private String employeeId;

	@Value("${app.security.initial-admin.first-name:}")
	private String firstName;

	@Value("${app.security.initial-admin.last-name:}")
	private String lastName;

	@Value("${app.security.initial-admin.email:}")
	private String email;

	@Value("${app.security.initial-admin.username:}")
	private String username;

	@Value("${app.security.initial-admin.password:}")
	private String password;

	@Value("${app.security.initial-admin.preferred-locale:EN}")
	private String preferredLocale;

	private final UserRepository userRepository;
	private final RoleRepository roleRepository;
	private final PasswordEncoder passwordEncoder;

	public InitialAdminUserSeeder(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
		this.userRepository = userRepository;
		this.roleRepository = roleRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		if (!initialAdminEnabled) {
			return;
		}

		if (isBlank(employeeId) || isBlank(firstName) || isBlank(lastName) || isBlank(email) || isBlank(password)) {
			log.warn("Initial admin seeding is enabled but required values are missing. Expected employee ID, first name, last name, email, and password.");
			return;
		}

		String normalizedEmployeeId = employeeId.trim();
		String normalizedEmail = email.trim().toLowerCase();
		String normalizedUsername = resolveUsername(normalizedEmployeeId);

		if (userRepository.findByEmployeeId(normalizedEmployeeId).isPresent()
			|| userRepository.findByEmail(normalizedEmail).isPresent()
			|| (normalizedUsername != null && userRepository.findByUsername(normalizedUsername).isPresent())) {
			log.info("Initial admin seed skipped because a matching user already exists.");
			return;
		}

		boolean existingAdminPresent = userRepository.findAll().stream()
			.anyMatch(user -> user.getRole() != null && UserRole.ADMIN.name().equalsIgnoreCase(user.getRole().getName()));
		if (existingAdminPresent) {
			log.info("Initial admin seed skipped because an ADMIN user already exists in the database.");
			return;
		}

		Role adminRole = roleRepository.findByName(UserRole.ADMIN.name())
			.orElseThrow(() -> new IllegalStateException("ADMIN role must exist before seeding the initial admin user."));

		LanguageCode locale = LanguageCode.valueOf(preferredLocale.trim().toUpperCase());
		User adminUser = new User(
			normalizedEmployeeId,
			firstName.trim(),
			lastName.trim(),
			normalizedEmail,
			normalizedUsername,
			passwordEncoder.encode(password),
			locale,
			UserSource.LOCAL,
			"ACTIVE",
			true,
			false,
			0,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			null,
			Instant.now(),
			null,
			null,
			null,
			null,
			adminRole
		);
		userRepository.save(adminUser);
		log.info("Initial ADMIN user seeded successfully for email {} and username {}", normalizedEmail, normalizedUsername);
	}

	private boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	private String resolveUsername(String normalizedEmployeeId) {
		if (username != null && !username.isBlank()) {
			return username.trim().toLowerCase();
		}
		return com.rra.taxhandbook.user.service.UsernameGenerator.generate(firstName, normalizedEmployeeId);
	}
}
