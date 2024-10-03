package com.notesbackend.dto;

import lombok.Data;

@Data
public class TagDto {
    private Long tid;
    private String name; 

    public TagDto(Long tid, String name) {
        this.tid = tid;
        this.name = name;
    }
}
