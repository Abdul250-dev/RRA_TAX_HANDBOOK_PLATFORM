package com.rra.taxhandbook.notification;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class SmtpEmailDeliveryService implements EmailDeliveryService {

	private static final Logger log = LoggerFactory.getLogger(SmtpEmailDeliveryService.class);

	private final JavaMailSender mailSender;

	@Value("${app.mail.from:no-reply@rra-tax-handbook.local}")
	private String fromEmail;

	@Value("${app.mail.invite-accept-url:http://localhost:3000/accept-invite?token={token}}")
	private String inviteAcceptUrl;

	@Value("${app.mail.reset-password-url:http://localhost:3000/reset-password?token={token}}")
	private String resetPasswordUrl;

	@Value("${app.mail.delivery.max-attempts:3}")
	private int maxAttempts = 3;

	@Value("${app.mail.delivery.retry-delay-ms:250}")
	private long retryDelayMs = 250L;

	public SmtpEmailDeliveryService(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	@Override
	public void sendInviteEmail(String recipientEmail, String fullName, String username, String inviteToken, String expiresAt) {
		String acceptUrl = buildTokenUrl(inviteAcceptUrl, inviteToken);
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(recipientEmail);
		message.setSubject("You're invited to RRA Tax Handbook");
		message.setText(
			"Hello " + fullName + ",\n\n" +
			"You have been invited to access the RRA Tax Handbook platform.\n" +
			(username == null || username.isBlank() ? "" : "Your username is: " + username + "\n\n") +
			"Accept your invitation here:\n" +
			acceptUrl + "\n\n" +
			"This invitation expires at: " + expiresAt + "\n\n" +
			"If you did not expect this email, you can ignore it and contact the platform administrator.\n"
		);
		sendWithRetry(message, "invite", recipientEmail);
	}

	@Override
	public void sendPasswordResetEmail(String recipientEmail, String fullName, String resetToken, String expiresAt) {
		String passwordResetUrl = buildTokenUrl(resetPasswordUrl, resetToken);
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(fromEmail);
		message.setTo(recipientEmail);
		message.setSubject("RRA Tax Handbook password reset");
		message.setText(
			"Hello " + fullName + ",\n\n" +
			"A password reset was requested for your account.\n" +
			"Reset your password here:\n" +
			passwordResetUrl + "\n\n" +
			"This reset link expires at: " + expiresAt + "\n\n" +
			"If you did not request this change, you should ignore this email and alert the platform administrator.\n"
		);
		sendWithRetry(message, "password reset", recipientEmail);
	}

	private String buildTokenUrl(String template, String token) {
		String encodedToken = URLEncoder.encode(token, StandardCharsets.UTF_8);
		if (template.contains("{token}")) {
			return template.replace("{token}", encodedToken);
		}
		String separator = template.contains("?") ? "&" : "?";
		return template + separator + "token=" + encodedToken;
	}

	private void sendWithRetry(SimpleMailMessage message, String messageType, String recipientEmail) {
		int configuredAttempts = Math.max(1, maxAttempts);
		RuntimeException lastFailure = null;
		for (int attempt = 1; attempt <= configuredAttempts; attempt++) {
			try {
				mailSender.send(message);
				if (attempt > 1) {
					log.info("Mail delivery recovered on retry {} of {} for {} email to {}", attempt, configuredAttempts, messageType, recipientEmail);
				}
				return;
			}
			catch (RuntimeException ex) {
				lastFailure = ex;
				if (attempt == configuredAttempts) {
					log.error("Mail delivery failed after {} attempts for {} email to {}", configuredAttempts, messageType, recipientEmail, ex);
					break;
				}
				log.warn("Mail delivery attempt {} of {} failed for {} email to {}. Retrying.", attempt, configuredAttempts, messageType, recipientEmail, ex);
				pauseBeforeRetry();
			}
		}

		throw lastFailure;
	}

	private void pauseBeforeRetry() {
		if (retryDelayMs <= 0) {
			return;
		}
		try {
			Thread.sleep(retryDelayMs);
		}
		catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Mail retry delay was interrupted.", ex);
		}
	}
}
