package com.notesbackend.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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

@Table(name = "notes", uniqueConstraints = @UniqueConstraint(columnNames = {"uid", "title"}))
@Entity
@Data
public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long nid;

    @NotNull
    private String title;

    private String body;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private boolean isPublic = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<EditHistory> editHistories;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uid", nullable = false)
    @JsonBackReference
    private User user;

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<NoteMedia> media = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Collaborator> collaborators = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Favorite> favorites = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<AuditLog> auditLogs = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = {CascadeType.ALL}, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<NoteTag> noteTags = new ArrayList<>();

    public void addTag(Tag tag) {
        NoteTag noteTag = new NoteTag();
        noteTag.setNote(this);
        noteTag.setTag(tag);
        noteTags.add(noteTag);
    }

    public void removeTag(Tag tag) {
        noteTags.removeIf(nt -> {
        	if (nt.getTag().equals(tag)) {
        		nt.setNote(null);
        		nt.setTag(null);
        		return true;
        	}
        	return false;
        });
    }

    public void addMedia(NoteMedia mediaItem) {
        mediaItem.setNote(this);
        media.add(mediaItem);
    }

    public void removeMedia(NoteMedia mediaItem) {
    	if (media.remove(mediaItem)) {
    		mediaItem.setNote(null);	
    	}
    }

    public void addCollaborator(Collaborator collaborator) {
        collaborator.setNote(this);
        collaborators.add(collaborator);
    }

    public void removeCollaborator(Collaborator collaborator) {
        if (collaborators.remove(collaborator)) {
        	collaborator.setNote(null);
        	
        }
    }

    public void addComment(Comment comment) {
        comment.setNote(this);
        comments.add(comment);
    }

    public void removeComment(Comment comment) {
    	if (comments.remove(comment)) {
    		comment.setNote(null);	
    	}
    }

    // Methods to add/remove favorites
    public void addFavorite(Favorite favorite) {
        favorite.setNote(this);
        favorites.add(favorite);
    }

    public void removeFavorite(Favorite favorite) {
    	if (favorites.remove(favorite)) {
    		favorite.setNote(null);	
    	}
    }

    // Method to toggle privacy
    public void togglePrivacy() {
        this.isPublic = !this.isPublic;
    }
}
