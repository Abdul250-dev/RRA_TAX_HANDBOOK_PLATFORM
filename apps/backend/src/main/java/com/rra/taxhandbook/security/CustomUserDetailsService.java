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
		String normalizedUsername = username == null ? "" : username.trim().toLowerCase();
		var localUser = userRepository.findByEmail(normalizedUsername)
			.or(() -> userRepository.findByUserCode(username == null ? "" : username.trim()));
		if (localUser.isPresent()) {
			var user = localUser.get();
			if (!"ACTIVE".equalsIgnoreCase(user.getStatus()) || user.getPasswordHash() == null || user.getPasswordHash().isBlank()) {
				throw new UsernameNotFoundException("User is not active for login: " + username);
			}
			return new User(
				user.getEmail(),
				user.getPasswordHash(),
				List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().getName()))
			);
		}

		if (!defaultUsername.equals(username)) {
			throw new UsernameNotFoundException("User not found: " + username);
		}

		return new User(defaultUsername, passwordEncoder.encode(defaultPassword), List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
	}
}
