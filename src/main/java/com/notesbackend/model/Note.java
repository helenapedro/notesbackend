package com.notesbackend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Entity
@Table(name = "notes", uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "title"}))
public class Note {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nid;

	@NotNull
    private String title;

    //@Column(columnDefinition = "TEXT")
    private String body;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ManyToOne
    @JoinColumn(name = "uid", nullable = false)
    @JsonBackReference
    private User user;

    private boolean isPublic; 

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<NoteTag> noteTags = new ArrayList<>();

    // Method to add a tag
    public void addTag(Tag tag) {
        NoteTag noteTag = new NoteTag();
        noteTag.setNote(this);
        noteTag.setTag(tag);
        noteTags.add(noteTag);
    }

    // Method to remove a tag
    public void removeTag(Tag tag) {
        noteTags.removeIf(nt -> nt.getTag().equals(tag));
    }
}
