package com.notesbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.notesbackend.model.NoteTag;

import java.util.List;

public interface NoteTagRepository extends JpaRepository<NoteTag, Long> {
    List<NoteTag> findByNoteNid(Long noteId);
    List<NoteTag> findByTagTid(Long tagId);
}