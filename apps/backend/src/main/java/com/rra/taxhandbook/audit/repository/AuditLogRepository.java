package com.rra.taxhandbook.audit.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rra.taxhandbook.audit.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	List<AuditLog> findTop20ByTargetEmailIgnoreCaseOrderByCreatedAtDesc(String targetEmail);
}
