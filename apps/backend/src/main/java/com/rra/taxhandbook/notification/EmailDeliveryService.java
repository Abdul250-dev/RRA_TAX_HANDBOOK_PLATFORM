package com.rra.taxhandbook.notification;

public interface EmailDeliveryService {

	void sendInviteEmail(String recipientEmail, String fullName, String inviteToken, String expiresAt);

	void sendPasswordResetEmail(String recipientEmail, String fullName, String resetToken, String expiresAt);
}
