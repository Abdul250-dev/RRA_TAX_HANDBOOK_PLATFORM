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

@Service
public class CustomUserDetailsService implements UserDetailsService {

	@Value("${spring.security.user.name:admin}")
	private String defaultUsername;

	@Value("${spring.security.user.password:Admin@123}")
	private String defaultPassword;

	private final PasswordEncoder passwordEncoder;

	public CustomUserDetailsService(PasswordEncoder passwordEncoder) {
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		if (!defaultUsername.equals(username)) {
			throw new UsernameNotFoundException("User not found: " + username);
		}

		return new User(defaultUsername, passwordEncoder.encode(defaultPassword), List.of(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN")));
	}
}
