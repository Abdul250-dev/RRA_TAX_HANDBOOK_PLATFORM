package com.rra.taxhandbook.user.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.common.dto.ApiResponse;
import com.rra.taxhandbook.audit.dto.UserActivityResponse;
import com.rra.taxhandbook.user.dto.AcceptInviteRequest;
import com.rra.taxhandbook.user.dto.InviteUserRequest;
import com.rra.taxhandbook.user.dto.PendingInviteResponse;
import com.rra.taxhandbook.user.dto.UpdateUserProfileRequest;
import com.rra.taxhandbook.user.dto.UpdateUserRoleRequest;
import com.rra.taxhandbook.user.dto.UserRequest;
import com.rra.taxhandbook.user.dto.UserInviteResponse;
import com.rra.taxhandbook.user.dto.UserResponse;
import com.rra.taxhandbook.user.dto.UserSummaryResponse;
import com.rra.taxhandbook.user.service.UserService;

@RestController
@RequestMapping("/api/users")
public class UserController {

	private final UserService userService;

	public UserController(UserService userService) {
		this.userService = userService;
	}

	@GetMapping
	@PreAuthorize("hasRole('ADMIN')")
	public List<UserResponse> getUsers(
		@RequestParam(required = false) String status,
		@RequestParam(required = false) String search,
		@RequestParam(required = false, defaultValue = "0") int page,
		@RequestParam(required = false, defaultValue = "10") int pageSize
	) {
		return userService.getUsers(status, search, page, pageSize);
	}

	@GetMapping("/invited")
	@PreAuthorize("hasRole('ADMIN')")
	public List<PendingInviteResponse> getPendingInvites() {
		return userService.getPendingInvites();
	}

	@GetMapping("/summary")
	@PreAuthorize("hasRole('ADMIN')")
	public UserSummaryResponse getUserSummary() {
		return userService.getUserSummary();
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public UserResponse getUser(@PathVariable Long id) {
		return userService.getUserById(id);
	}

	@GetMapping("/{id}/activity")
	@PreAuthorize("hasAnyRole('ADMIN','AUDITOR')")
	public List<UserActivityResponse> getUserActivity(@PathVariable Long id) {
		return userService.getUserActivity(id);
	}

	@PostMapping
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserResponse> createUser(@RequestBody UserRequest request, Authentication authentication) {
		return userService.createUser(request, authentication.getName());
	}

	@PostMapping("/invite")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserInviteResponse> inviteUser(@RequestBody InviteUserRequest request, Authentication authentication) {
		return userService.inviteUser(request, authentication.getName());
	}

	@PostMapping("/{id}/resend-invite")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserInviteResponse> resendInvite(@PathVariable Long id, Authentication authentication) {
		return userService.resendInvite(id, authentication.getName());
	}

	@PostMapping("/{id}/resend")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserInviteResponse> resendInviteAlias(@PathVariable Long id, Authentication authentication) {
		return userService.resendInvite(id, authentication.getName());
	}

	@PostMapping("/{id}/cancel-invite")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<String> cancelInvite(@PathVariable Long id, Authentication authentication) {
		return userService.cancelInvite(id, authentication.getName());
	}

	@PostMapping("/{id}/cancel")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<String> cancelInviteAlias(@PathVariable Long id, Authentication authentication) {
		return userService.cancelInvite(id, authentication.getName());
	}

	@PostMapping("/accept-invite")
	public ApiResponse<UserResponse> acceptInvite(@RequestBody AcceptInviteRequest request) {
		return userService.acceptInvite(request);
	}

	@PostMapping("/{id}/role")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserResponse> updateUserRole(
		@PathVariable Long id,
		@RequestBody UpdateUserRoleRequest request,
		Authentication authentication
	) {
		return userService.updateUserRole(id, request, authentication.getName());
	}

	@PostMapping("/{id}/profile")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserResponse> updateUserProfile(
		@PathVariable Long id,
		@RequestBody UpdateUserProfileRequest request,
		Authentication authentication
	) {
		return userService.updateUserProfile(id, request, authentication.getName());
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<String> removeUser(@PathVariable Long id, Authentication authentication) {
		return userService.removeUser(id, authentication.getName());
	}

	@PostMapping("/{id}/remove")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<String> removeUserAlias(@PathVariable Long id, Authentication authentication) {
		return userService.removeUser(id, authentication.getName());
	}

	@PostMapping("/{id}/suspend")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserResponse> suspendUser(@PathVariable Long id, Authentication authentication) {
		return userService.suspendUser(id, authentication.getName());
	}

	@PostMapping("/{id}/deactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserResponse> deactivateUserAlias(@PathVariable Long id, Authentication authentication) {
		return userService.suspendUser(id, authentication.getName());
	}

	@PostMapping("/{id}/reactivate")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserResponse> reactivateUser(@PathVariable Long id, Authentication authentication) {
		return userService.reactivateUser(id, authentication.getName());
	}

	@PostMapping("/{id}/restore")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<UserInviteResponse> restoreUser(@PathVariable Long id, Authentication authentication) {
		return userService.restoreUser(id, authentication.getName());
	}
}
