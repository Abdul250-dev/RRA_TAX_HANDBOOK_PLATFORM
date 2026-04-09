package com.rra.taxhandbook.user.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.user.dto.AcceptInviteRequest;
import com.rra.taxhandbook.user.dto.AdminSetPasswordUserRequest;
import com.rra.taxhandbook.user.dto.InviteUserRequest;
import com.rra.taxhandbook.user.dto.PendingInviteResponse;
import com.rra.taxhandbook.user.dto.UpdateUserProfileRequest;
import com.rra.taxhandbook.user.dto.UpdateUserRoleRequest;
import com.rra.taxhandbook.user.dto.UserRequest;
import com.rra.taxhandbook.user.dto.UserInviteResponse;
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
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public List<UserResponse> getUsers() {
		return userService.getUsers();
	}

	@GetMapping("/invited")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public List<PendingInviteResponse> getPendingInvites() {
		return userService.getPendingInvites();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public UserResponse getUser(@PathVariable Long id) {
		return userService.getUserById(id);
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<UserResponse> createUser(@RequestBody UserRequest request) {
		return userService.createUser(request);
	}

	@PostMapping("/invite")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<UserInviteResponse> inviteUser(@RequestBody InviteUserRequest request) {
		return userService.inviteUser(request);
	}

	@PostMapping("/{id}/resend-invite")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<UserInviteResponse> resendInvite(@PathVariable Long id, Authentication authentication) {
		return userService.resendInvite(id, authentication.getName());
	}

	@PostMapping("/{id}/cancel-invite")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<String> cancelInvite(@PathVariable Long id, Authentication authentication) {
		return userService.cancelInvite(id, authentication.getName());
	}

	@PostMapping("/accept-invite")
	public ApiResponse<UserResponse> acceptInvite(@RequestBody AcceptInviteRequest request) {
		return userService.acceptInvite(request);
	}

	@PostMapping("/admin-create")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<UserResponse> createUserWithPassword(@RequestBody AdminSetPasswordUserRequest request) {
		return userService.createUserWithPassword(request);
	}

	@PostMapping("/{id}/role")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<UserResponse> updateUserRole(
		@PathVariable Long id,
		@RequestBody UpdateUserRoleRequest request,
		Authentication authentication
	) {
		return userService.updateUserRole(id, request, authentication.getName());
	}

	@PostMapping("/{id}/profile")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<UserResponse> updateUserProfile(
		@PathVariable Long id,
		@RequestBody UpdateUserProfileRequest request,
		Authentication authentication
	) {
		return userService.updateUserProfile(id, request, authentication.getName());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<String> removeUser(@PathVariable Long id, Authentication authentication) {
		return userService.removeUser(id, authentication.getName());
	}

	@PostMapping("/{id}/restore")
	@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
	public ApiResponse<UserInviteResponse> restoreUser(@PathVariable Long id, Authentication authentication) {
		return userService.restoreUser(id, authentication.getName());
	}
}
