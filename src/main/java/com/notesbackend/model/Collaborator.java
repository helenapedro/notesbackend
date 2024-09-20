package com.notesbackend.model;

import org.hibernate.validator.constraints.UniqueElements;

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
@Table(name = "collaborators")
public class Collaborator {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long coll_id;

    @UniqueElements
    @ManyToOne
    @JoinColumn(name = "nid", nullable = false)
    private Note note;

    @UniqueElements
    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    private String permissionLevel;  // e.g., "READ", "WRITE"
}
