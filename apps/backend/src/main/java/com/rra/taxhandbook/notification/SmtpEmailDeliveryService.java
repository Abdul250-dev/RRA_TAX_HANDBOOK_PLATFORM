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

	public SmtpEmailDeliveryService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public void sendInviteEmail(String recipientEmail, String fullName, String inviteToken, String expiresAt) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(recipientEmail);
		message.setSubject("RRA Tax Handbook invitation");
		message.setText(
			"Hello " + fullName + ",\n\n" +
			"You have been invited to access the RRA Tax Handbook platform.\n" +
			"Invite token: " + inviteToken + "\n" +
			"Expires at: " + expiresAt + "\n\n" +
			"For development, you can use this token directly in the accept-invite API.\n"
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
