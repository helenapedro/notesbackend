package com.notesbackend.dto;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EditHistoryDto {

    private Long editId;
    private Long nid;
    private Long uid;
    private String changedField;
    private String oldValue;
    private String newValue;
    private LocalDateTime timestamp;

    public EditHistoryDto(Long editId, Long nid, Long uid, String changedField, String oldValue, String newValue ) {
        this.editId = editId;
        this.nid = nid;
        this.uid = uid;
        this.changedField = changedField;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
}
