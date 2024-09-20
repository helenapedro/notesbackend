package com.notesbackend.model;


import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "note_media")
public class NoteMedia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mid;

    private String fileName;
    
    private String fileType; // e.g., "image/jpeg", "audio/mpeg"
    
    private String s3Url; // URL to the media file in AWS S3

    @ManyToOne
    @JoinColumn(name = "nid", nullable = false)
    private Note note; // Link to the associated note

    private Long uploadedBy;

    private boolean isImage; // true if it's an image, false if it's an audio file
    
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onUpload() {
        uploadedAt = LocalDateTime.now();
    }
}