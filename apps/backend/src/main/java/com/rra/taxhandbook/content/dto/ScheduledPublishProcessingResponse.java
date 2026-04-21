package com.rra.taxhandbook.content.dto;

import java.util.List;

public record ScheduledPublishProcessingResponse(
	int processedCount,
	int skippedCount,
	List<Long> processedTopicIds,
	List<SkippedScheduledPublishResponse> skippedTopics
) {
	public record SkippedScheduledPublishResponse(
		Long topicId,
		List<String> reasons
	) {
	}
}
