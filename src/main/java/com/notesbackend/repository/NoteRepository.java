package com.notesbackend.repository;

import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.notesbackend.model.Note;

public interface NoteRepository extends JpaRepository<Note, Long> {
    Page<Note> findByUser_Uid(Long userId, Pageable pageable);
    Page<Note> findByUserUidAndCreatedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
    Page<Note> findByIsPublicTrue(Pageable pageable);
}