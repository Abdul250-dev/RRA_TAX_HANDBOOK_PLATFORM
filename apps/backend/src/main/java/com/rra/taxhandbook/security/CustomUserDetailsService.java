package com.rra.taxhandbook.security;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.rra.taxhandbook.user.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Value("${spring.security.user.name:admin}")
	private String defaultUsername;

	@Value("${spring.security.user.password:Admin@123}")
	private String defaultPassword;

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	public CustomUserDetailsService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (username == null || username.trim().isEmpty()) {
			throw new UsernameNotFoundException("Username cannot be empty");
		}

		String normalizedUsername = username.trim().toLowerCase();
		
		// Try to find by email first
		var localUser = userRepository.findByEmail(normalizedUsername);
		
		// If not found, try by userCode (case-sensitive)
		if (localUser.isEmpty()) {
			localUser = userRepository.findByUserCode(username.trim());
		}
		
		if (localUser.isPresent()) {
			var user = localUser.get();
			if (!"ACTIVE".equalsIgnoreCase(user.getStatus())) {
				throw new UsernameNotFoundException("User is not active for login: " + username);
			}
			if (user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
				throw new UsernameNotFoundException("User has no password set: " + username);
			}
			return new User(
				user.getEmail(),
				user.getPasswordHash(),
				List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))
			);
		}

		// Fallback to default admin user only if username matches exactly
		if (defaultUsername.equals(username)) {
			return new User(defaultUsername, passwordEncoder.encode(defaultPassword), List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
		}

		throw new UsernameNotFoundException("User not found: " + username);
	}
}
