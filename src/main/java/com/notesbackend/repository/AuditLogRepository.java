package com.notesbackend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notesbackend.model.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	List<AuditLog> findByNoteNid(Long noteId);
	List<AuditLog> findByUserUid(Long userId);
}