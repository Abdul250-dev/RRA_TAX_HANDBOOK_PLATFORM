package com.rra.taxhandbook.notification;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.stereotype.Component;

@Component
public class MailConfigurationValidator implements InitializingBean {

	private final Environment environment;

	@Value("${app.mail.from:no-reply@rra-tax-handbook.local}")
	private String fromEmail;

	@Value("${app.mail.invite-accept-url:http://localhost:3000/accept-invite?token={token}}")
	private String inviteAcceptUrl;

	@Value("${app.mail.reset-password-url:http://localhost:3000/reset-password?token={token}}")
	private String resetPasswordUrl;

	public MailConfigurationValidator(Environment environment) {
		this.environment = environment;
	}

	@Override
	public void afterPropertiesSet() {
		validate(environment.acceptsProfiles(Profiles.of("prod")));
	}

	void validate(boolean productionProfile) {
		if (!productionProfile) {
			return;
		}
		requireConfiguredValue(fromEmail, "app.mail.from");
		requireConfiguredLink(inviteAcceptUrl, "app.mail.invite-accept-url");
		requireConfiguredLink(resetPasswordUrl, "app.mail.reset-password-url");
	}

	private void requireConfiguredValue(String value, String propertyName) {
		if (value == null || value.isBlank() || value.endsWith(".local")) {
			throw new IllegalStateException(propertyName + " must be configured with a production-safe value.");
		}
	}

	private void requireConfiguredLink(String url, String propertyName) {
		if (url == null || url.isBlank() || !url.contains("{token}")) {
			throw new IllegalStateException(propertyName + " must include a {token} placeholder.");
		}
		if (url.contains("localhost")) {
			throw new IllegalStateException(propertyName + " must not point to localhost in production.");
		}
	}
}
