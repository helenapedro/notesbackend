package com.notesbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.notesbackend.model.NoteMedia;
import java.util.List;

public interface NoteMediaRepository extends JpaRepository<NoteMedia, Long> {
    List<NoteMedia> findByNoteNid(Long noteId);
}
