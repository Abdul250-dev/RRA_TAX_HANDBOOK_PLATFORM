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

	@Value("${app.security.bootstrap-admin.enabled:false}")
	private boolean bootstrapAdminEnabled;

	@Value("${app.security.bootstrap-admin.username:}")
	private String bootstrapAdminUsername;

	@Value("${app.security.bootstrap-admin.password:}")
	private String bootstrapAdminPassword;

	private final PasswordEncoder passwordEncoder;
	private final UserRepository userRepository;

	public CustomUserDetailsService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
		this.passwordEncoder = passwordEncoder;
		this.userRepository = userRepository;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
		var localUser = userRepository.findByUsername(normalizedUsername);
		if (localUser.isPresent()) {
			var user = localUser.get();
			if (!"ACTIVE".equalsIgnoreCase(user.getStatus())
				|| !user.isActive()
				|| user.isLocked()
				|| user.getPasswordHash() == null
				|| user.getPasswordHash().isBlank()) {
				throw new UsernameNotFoundException("User is not active for login: " + username);
			}
			return new User(
				user.getUsername(),
				user.getPasswordHash(),
				List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))
			);
		}

		if (!bootstrapAdminEnabled || bootstrapAdminUsername == null || bootstrapAdminUsername.isBlank()) {
			throw new UsernameNotFoundException("User not found: " + username);
		}

		if (!bootstrapAdminUsername.equals(username) || bootstrapAdminPassword == null || bootstrapAdminPassword.isBlank()) {
			throw new UsernameNotFoundException("User not found: " + username);
		}

		return new User(bootstrapAdminUsername, passwordEncoder.encode(bootstrapAdminPassword), List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
	}
}
