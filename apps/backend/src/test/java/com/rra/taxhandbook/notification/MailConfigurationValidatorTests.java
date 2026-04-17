package com.rra.taxhandbook.notification;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;
import org.springframework.test.util.ReflectionTestUtils;

class MailConfigurationValidatorTests {

	@Test
	void productionValidationRejectsLocalInviteUrl() {
		MailConfigurationValidator validator = new MailConfigurationValidator(new MockEnvironment());
		ReflectionTestUtils.setField(validator, "fromEmail", "no-reply@rra.test");
		ReflectionTestUtils.setField(validator, "inviteAcceptUrl", "http://localhost:3000/accept-invite?token={token}");
		ReflectionTestUtils.setField(validator, "resetPasswordUrl", "https://admin.rra.test/reset-password?token={token}");

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validator.validate(true));

		assertEquals("app.mail.invite-accept-url must not point to localhost in production.", exception.getMessage());
	}

	@Test
	void productionValidationRejectsMissingTokenPlaceholder() {
		MailConfigurationValidator validator = new MailConfigurationValidator(new MockEnvironment());
		ReflectionTestUtils.setField(validator, "fromEmail", "no-reply@rra.test");
		ReflectionTestUtils.setField(validator, "inviteAcceptUrl", "https://admin.rra.test/accept-invite");
		ReflectionTestUtils.setField(validator, "resetPasswordUrl", "https://admin.rra.test/reset-password?token={token}");

		IllegalStateException exception = assertThrows(IllegalStateException.class, () -> validator.validate(true));

		assertEquals("app.mail.invite-accept-url must include a {token} placeholder.", exception.getMessage());
	}

	@Test
	void nonProductionValidationAllowsLocalDefaults() {
		MailConfigurationValidator validator = new MailConfigurationValidator(new MockEnvironment());
		ReflectionTestUtils.setField(validator, "fromEmail", "no-reply@rra-tax-handbook.local");
		ReflectionTestUtils.setField(validator, "inviteAcceptUrl", "http://localhost:3000/accept-invite?token={token}");
		ReflectionTestUtils.setField(validator, "resetPasswordUrl", "http://localhost:3000/reset-password?token={token}");

		assertDoesNotThrow(() -> validator.validate(false));
	}
}
