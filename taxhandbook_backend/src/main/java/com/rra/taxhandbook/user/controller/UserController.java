package com.rra.taxhandbook.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.user.dto.UserRequest;
import com.rra.taxhandbook.user.dto.UserResponse;
import com.rra.taxhandbook.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	public List<UserResponse> getUsers() {
		return userService.getUsers();
	}

	@GetMapping("/{id}")
	public UserResponse getUser(@PathVariable Long id) {
		return userService.getUserById(id);
	}

	@PostMapping
	public ApiResponse<UserResponse> createUser(@RequestBody UserRequest request) {
		return userService.createUser(request);
	}
}
