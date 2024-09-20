package com.notesbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.notesbackend.model.Collaborator;
import java.util.List;

public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {
    List<Collaborator> findByNoteNid(Long noteId);
    List<Collaborator> findByUserUid(Long userId);
}