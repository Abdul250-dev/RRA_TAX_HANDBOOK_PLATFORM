package com.rra.taxhandbook.common.util;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public final class DateUtil {

	private static final DateTimeFormatter FORMATTER =
		DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.of("Africa/Kigali"));

	private DateUtil() {
	}

	public static String formatInstant(Instant instant) {
		return FORMATTER.format(instant);
	}
}
