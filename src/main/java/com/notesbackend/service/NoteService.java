package com.notesbackend.service;

import com.notesbackend.repository.NoteRepository;
import com.notesbackend.model.Note;
import com.notesbackend.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NoteService {

    @Autowired
    private NoteRepository noteRepository;

    @PreAuthorize("isAuthenticated()")
    public Page<Note> getNotesByUserAndDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate == null && endDate == null) {
            return noteRepository.findByUser_Uid(userId, pageable);
        }
        if (startDate == null) {
            startDate = LocalDateTime.MIN;
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }
        return noteRepository.findByUserUidAndCreatedAtBetween(userId, startDate, endDate, pageable);
    }

    public Page<Note> getPublicNotes(Pageable pageable) {
        return noteRepository.findByIsPublicTrue(pageable);
    }
    
    @Cacheable(value = "notes", key = "#userId + '-' + #id")
    public Note getNoteById(Long id, Long userId) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note != null && note.getUser().getUid().equals(userId)) {
        	return note;
        }
        return null;
    }

    //@PreAuthorize("isAuthenticated()")
    @PreAuthorize("#user.uid == principal.uid")
    public Note createNote(Note note, User user) {
        note.setUser(user);
        note.setCreatedAt(LocalDateTime.now());
        return noteRepository.save(note);
    }

    @CachePut(value = "notes", key = "#user.uid + '-' + #id")
    public Note updateNote(Long id, Note updatedNote, User user) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note != null && note.getUser().getUid().equals(user.getUid())) {
            note.setTitle(updatedNote.getTitle());
            note.setBody(updatedNote.getBody());
            note.setUpdatedAt(LocalDateTime.now());
            return noteRepository.save(note);
        }
        return null;
    }

    @CacheEvict(value = "notes", key = "#userId + '-' + #id")
    public boolean deleteNoteById(Long id, User user) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note != null && note.getUser().getUid().equals(user.getUid())) {
            noteRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
