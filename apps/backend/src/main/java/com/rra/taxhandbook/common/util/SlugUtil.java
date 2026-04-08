package com.rra.taxhandbook.common.util;

public final class SlugUtil {

	private SlugUtil() {
	}

	public static String toSlug(String value) {
		return value == null ? "" : value.trim().toLowerCase().replace(" ", "-");
	}
}
