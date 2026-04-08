package com.rra.taxhandbook.audit.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rra.taxhandbook.audit.entity.AuditLog;
import com.rra.taxhandbook.audit.service.AuditLogService;

@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {

	private final AuditLogService auditLogService;

	public AuditLogController(AuditLogService auditLogService) {
		this.auditLogService = auditLogService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('AUDITOR','SUPER_ADMIN')")
	public List<AuditLog> getAuditLogs() {
		return auditLogService.getAuditLogs();
	}
}
