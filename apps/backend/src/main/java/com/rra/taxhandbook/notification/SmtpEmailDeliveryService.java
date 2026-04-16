package com.rra.taxhandbook.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailDeliveryService implements EmailDeliveryService {

	private final JavaMailSender mailSender;

	@Value("${app.mail.from:no-reply@rra-tax-handbook.local}")
	private String fromEmail;

	@Value("${app.frontend.base-url:http://localhost:3000}")
	private String frontendBaseUrl;

	public SmtpEmailDeliveryService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public void sendInviteEmail(String recipientEmail, String fullName, String inviteToken, String expiresAt) {
		String inviteLink = frontendBaseUrl + "/invitations/accept?token=" + inviteToken;
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(recipientEmail);
		message.setSubject("You're invited to RRA Tax Handbook");
		message.setText(
			"Hello " + fullName + ",\n\n" +
			"You have been invited to access the RRA Tax Handbook platform.\n\n" +
			"Click the link below to set your password and activate your account:\n" +
			inviteLink + "\n\n" +
			"This link expires at: " + expiresAt + "\n\n" +
			"If you did not expect this invitation, please ignore this email.\n"
		);
		mailSender.send(message);
	}

	@Override
	public void sendPasswordResetEmail(String recipientEmail, String fullName, String resetToken, String expiresAt) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(recipientEmail);
		message.setSubject("RRA Tax Handbook password reset");
		message.setText(
			"Hello " + fullName + ",\n\n" +
			"A password reset was requested for your account.\n" +
			"Reset token: " + resetToken + "\n" +
			"Expires at: " + expiresAt + "\n\n" +
			"For development, you can use this token directly in the reset-password API.\n"
		);
		mailSender.send(message);
	}
}
