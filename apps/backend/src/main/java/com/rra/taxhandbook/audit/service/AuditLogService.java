package com.rra.taxhandbook.audit.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.audit.dto.UserActivityResponse;
import com.rra.taxhandbook.audit.entity.AuditLog;
import com.rra.taxhandbook.audit.repository.AuditLogRepository;

@Service
public class AuditLogService {

	private final AuditLogRepository auditLogRepository;

	public AuditLogService(AuditLogRepository auditLogRepository) {
		this.auditLogRepository = auditLogRepository;
	}

	public List<AuditLog> getAuditLogs() {
		return auditLogRepository.findAll();
	}

	public List<UserActivityResponse> getRecentUserActivity(String email) {
		return auditLogRepository.findTop20ByTargetEmailIgnoreCaseOrderByCreatedAtDesc(email).stream()
			.map(auditLog -> new UserActivityResponse(
				auditLog.getId(),
				auditLog.getAction(),
				auditLog.getActor(),
				auditLog.getTargetEmail(),
				auditLog.getDetails(),
				auditLog.getCreatedAt().toString()
			))
			.toList();
	}

	public void log(String action, String actor, String targetEmail, String details) {
		auditLogRepository.save(new AuditLog(action, actor, targetEmail, details, Instant.now()));
	}
}
