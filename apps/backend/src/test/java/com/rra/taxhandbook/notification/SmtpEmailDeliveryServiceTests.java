package com.rra.taxhandbook.notification;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SmtpEmailDeliveryServiceTests {

	@Mock
	private JavaMailSender mailSender;

	@Test
	void inviteEmailContainsClickableAcceptLink() {
		SmtpEmailDeliveryService service = new SmtpEmailDeliveryService(mailSender);
		ReflectionTestUtils.setField(service, "fromEmail", "no-reply@rra.test");
		ReflectionTestUtils.setField(service, "inviteAcceptUrl", "https://admin.rra.test/accept-invite?token={token}");
		ReflectionTestUtils.setField(service, "resetPasswordUrl", "https://admin.rra.test/reset-password?token={token}");
		ReflectionTestUtils.setField(service, "retryDelayMs", 0L);

		service.sendInviteEmail("invitee@rra.test", "Invitee User", "inviteeuser001", "invite-token-123", "2026-04-16T12:00:00Z");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());
		String body = captor.getValue().getText();

		assertTrue(body.contains("https://admin.rra.test/accept-invite?token=invite-token-123"));
		assertTrue(body.contains("Your username is: inviteeuser001"));
		assertTrue(body.contains("This invitation expires at: 2026-04-16T12:00:00Z"));
	}

	@Test
	void resetEmailContainsClickableResetLink() {
		SmtpEmailDeliveryService service = new SmtpEmailDeliveryService(mailSender);
		ReflectionTestUtils.setField(service, "fromEmail", "no-reply@rra.test");
		ReflectionTestUtils.setField(service, "inviteAcceptUrl", "https://admin.rra.test/accept-invite?token={token}");
		ReflectionTestUtils.setField(service, "resetPasswordUrl", "https://admin.rra.test/reset-password?token={token}");
		ReflectionTestUtils.setField(service, "retryDelayMs", 0L);

		service.sendPasswordResetEmail("reset@rra.test", "Reset User", "reset-token-456", "2026-04-16T13:00:00Z");

		ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
		verify(mailSender).send(captor.capture());
		String body = captor.getValue().getText();

		assertTrue(body.contains("https://admin.rra.test/reset-password?token=reset-token-456"));
		assertTrue(body.contains("This reset link expires at: 2026-04-16T13:00:00Z"));
	}

	@Test
	void inviteEmailRetriesBeforeSucceeding() {
		SmtpEmailDeliveryService service = new SmtpEmailDeliveryService(mailSender);
		ReflectionTestUtils.setField(service, "fromEmail", "no-reply@rra.test");
		ReflectionTestUtils.setField(service, "inviteAcceptUrl", "https://admin.rra.test/accept-invite?token={token}");
		ReflectionTestUtils.setField(service, "resetPasswordUrl", "https://admin.rra.test/reset-password?token={token}");
		ReflectionTestUtils.setField(service, "maxAttempts", 2);
		ReflectionTestUtils.setField(service, "retryDelayMs", 0L);

		doThrow(new RuntimeException("smtp unavailable"))
			.doNothing()
			.when(mailSender)
			.send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

		service.sendInviteEmail("invitee@rra.test", "Invitee User", "inviteeuser001", "invite-token-123", "2026-04-16T12:00:00Z");

		verify(mailSender, times(2)).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
	}

	@Test
	void resetEmailThrowsAfterConfiguredAttemptsAreExhausted() {
		SmtpEmailDeliveryService service = new SmtpEmailDeliveryService(mailSender);
		ReflectionTestUtils.setField(service, "fromEmail", "no-reply@rra.test");
		ReflectionTestUtils.setField(service, "inviteAcceptUrl", "https://admin.rra.test/accept-invite?token={token}");
		ReflectionTestUtils.setField(service, "resetPasswordUrl", "https://admin.rra.test/reset-password?token={token}");
		ReflectionTestUtils.setField(service, "maxAttempts", 2);
		ReflectionTestUtils.setField(service, "retryDelayMs", 0L);

		doThrow(new RuntimeException("smtp unavailable"))
			.when(mailSender)
			.send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));

		assertThrows(RuntimeException.class,
			() -> service.sendPasswordResetEmail("reset@rra.test", "Reset User", "reset-token-456", "2026-04-16T13:00:00Z"));

		verify(mailSender, times(2)).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
	}
}
