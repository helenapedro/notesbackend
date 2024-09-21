package com.notesbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.notesbackend.model.Collaborator;
import com.notesbackend.model.Note;
import com.notesbackend.model.User;

import java.util.List;

public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {
    List<Collaborator> findByNoteNid(Long noteId);
    List<Collaborator> findByUserUid(Long userId);
    List<Collaborator> deleteByNoteAndUserUid(Note note, Long userId);
	boolean existsByNoteAndUser(Note note, User user);
	List<Collaborator> findByNote(Note note);
}