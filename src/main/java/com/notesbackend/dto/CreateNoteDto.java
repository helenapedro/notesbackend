package com.notesbackend.dto;

import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CreateNoteDto {
    
    private Long nid;

    @NotBlank(message = "Title is required")
    @Size(max = 100, message = "Title cannot exceed 100 characters")
    private String title;

    @Size(max = 1000, message = "Body cannot exceed 1000 characters")
    private String body;
    
    private List<TagDto> tags;

    // Default constructor
    public CreateNoteDto() {
    }

    // Parameterized constructor
    public CreateNoteDto(Long nid, String title, String body, List<TagDto> tags) {
        this.nid = nid;
        this.title = title;
        this.body = body;
        this.tags = tags;
    }
}
