package com.rra.taxhandbook.audit.repository;

import java.util.List;

import com.rra.taxhandbook.audit.entity.AuditLog;

public interface AuditLogRepository {

	List<AuditLog> findAll();
}
