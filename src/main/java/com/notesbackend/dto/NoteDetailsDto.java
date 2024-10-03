package com.notesbackend.dto;

import java.util.Date;
import java.util.List;
import lombok.Data;

@Data
public class NoteDetailsDto {

    private Long nid;
    private String title;
    private String body;
    private List<TagDto> tags;
    private Date createdAt;
    private Date updatedAt;
    private Boolean isPublic;

    public NoteDetailsDto() {}

    public NoteDetailsDto(Long nid, String title, String body, List<TagDto> tags, Date createdAt, Date updatedAt, Boolean isPublic) {
        this.nid = nid;
        this.title = title;
        this.body = body;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.isPublic = isPublic;
    }
}
