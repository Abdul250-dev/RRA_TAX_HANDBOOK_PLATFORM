package com.rra.taxhandbook.user.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.common.enums.UserRole;
import com.rra.taxhandbook.user.dto.UserRequest;
import com.rra.taxhandbook.user.dto.UserResponse;

@Service
public class UserService {

	public List<UserResponse> getUsers() {
		return List.of(new UserResponse(1L, "System Administrator", "admin@rra.gov.rw", UserRole.ADMIN));
	}

	public UserResponse getUserById(Long id) {
		return new UserResponse(id, "System Administrator", "admin@rra.gov.rw", UserRole.ADMIN);
	}

	public ApiResponse<UserResponse> createUser(UserRequest request) {
		UserResponse response = new UserResponse(2L, request.fullName(), request.email(), request.role());
		return new ApiResponse<>("User scaffold created", response);
	}
}
