package com.rra.taxhandbook.user.dto;

public record AcceptInviteRequest(
	String token,
	String password
) {
}
