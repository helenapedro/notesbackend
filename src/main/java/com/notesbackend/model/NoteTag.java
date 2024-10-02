package com.notesbackend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "note_tags")
@Data
public class NoteTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ntid;

    @ManyToOne
    @JoinColumn(name = "nid", nullable = false)
    private Note note;

    @ManyToOne
    @JoinColumn(name = "tid", nullable = false)
    private Tag tag;
}
