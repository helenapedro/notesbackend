package com.notesbackend.service;

import com.notesbackend.model.AuditLog;
import com.notesbackend.model.Note;
import com.notesbackend.model.User;
import com.notesbackend.repository.AuditLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class AuditLogService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    public void logAction(User user, Note note, String action) {
        AuditLog auditLog = new AuditLog();
        auditLog.setUser(user);
        auditLog.setNote(note);
        auditLog.setAction(action);
        auditLog.setTimestamp(LocalDateTime.now());
        auditLogRepository.save(auditLog);
    }
}
