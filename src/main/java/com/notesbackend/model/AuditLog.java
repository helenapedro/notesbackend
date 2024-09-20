package com.notesbackend.model;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "audit_logs")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long audtid;
    
    private String action;  // e.g., "VIEWED", "EDITED", "DELETED"
    private LocalDateTime timestamp;

    @ManyToOne
    @JoinColumn(name = "nid", nullable = false)
    private Note note;

    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private User user;
}

