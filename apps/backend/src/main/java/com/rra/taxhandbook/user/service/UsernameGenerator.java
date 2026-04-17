package com.rra.taxhandbook.user.service;

import java.util.Locale;

public final class UsernameGenerator {

	private UsernameGenerator() {
	}

	public static String generate(String firstName, String employeeId) {
		if (firstName == null || firstName.isBlank()) {
			throw new IllegalArgumentException("First name is required to generate username.");
		}
		if (employeeId == null || employeeId.isBlank()) {
			throw new IllegalArgumentException("Employee ID is required to generate username.");
		}

		String normalizedPrefix = firstName.trim()
			.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]", "");
		if (normalizedPrefix.isBlank()) {
			throw new IllegalArgumentException("First name must contain at least one letter or digit to generate username.");
		}

		String normalizedEmployeeId = employeeId.trim()
			.toLowerCase(Locale.ROOT)
			.replaceAll("[^a-z0-9]", "");
		if (normalizedEmployeeId.isBlank()) {
			throw new IllegalArgumentException("Employee ID must contain at least one letter or digit to generate username.");
		}

		String prefix = normalizedPrefix.substring(0, Math.min(3, normalizedPrefix.length()));
		return prefix + normalizedEmployeeId;
	}
}
