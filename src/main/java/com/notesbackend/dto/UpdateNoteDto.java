package com.notesbackend.dto;

import java.util.List;
import lombok.Data;

@Data
public class UpdateNoteDto {

    private Long nid;
    private String title;  
    private String body;   
    private List<TagDto> tags;

    // Default constructor
    public UpdateNoteDto() {}

    // Parameterized constructor
    public UpdateNoteDto(Long nid, String title, String body, List<TagDto> tags) {
        this.nid = nid;
        this.title = title;
        this.body = body;
        this.tags = tags;
    }
}
