package com.rra.taxhandbook.audit.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.rra.taxhandbook.audit.entity.AuditLog;
import com.rra.taxhandbook.common.util.DateUtil;

@Service
public class AuditLogService {

	public List<AuditLog> getAuditLogs() {
		return List.of(new AuditLog(1L, "LOGIN", "admin", DateUtil.formatInstant(Instant.now())));
	}
}
