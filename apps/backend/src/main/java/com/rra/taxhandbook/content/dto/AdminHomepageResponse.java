package com.rra.taxhandbook.content.dto;

import java.time.Instant;
import java.util.List;

public record AdminHomepageResponse(
	String kicker,
	String title,
	String subtitle,
	String searchLabel,
	String helpLabel,
	String status,
	Instant updatedAt,
	List<HomepageCardResponse> cards
) {}
