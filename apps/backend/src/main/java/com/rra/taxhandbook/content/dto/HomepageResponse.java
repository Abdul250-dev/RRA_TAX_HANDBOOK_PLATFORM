package com.rra.taxhandbook.content.dto;

import java.time.Instant;
import java.util.List;

public record HomepageResponse(
	String kicker,
	String title,
	String subtitle,
	String searchLabel,
	String helpLabel,
	Instant updatedAt,
	List<HomepageCardResponse> cards
) {}
