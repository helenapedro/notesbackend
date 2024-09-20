package com.notesbackend.model;


import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;


@Data
@Entity
@Table(name = "note_media")
@EqualsAndHashCode(exclude = {"note", "uploadedBy"})
@ToString(exclude = {"note", "uploadedBy"})
public class NoteMedia {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long mid;

    private String fileName;
    
    private String fileType; // e.g., "image/jpeg", "audio/mpeg"
    
    private String s3Url;

    @ManyToOne
    @JoinColumn(name = "nid", nullable = false)
    private Note note; 

    @ManyToOne
    @JoinColumn(name = "uploaded_by", nullable = false)
    private User uploadedBy;

    private boolean isImage; // true if it's an image, false if it's an audio file
    
    private long fileSize;
    
    private LocalDateTime uploadedAt;

    @PrePersist
    protected void onUpload() {
        uploadedAt = LocalDateTime.now();
    }
}