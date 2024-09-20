package com.notesbackend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.notesbackend.model.Comment;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByNoteNid(Long noteId);
    List<Comment> findByUserUid(Long userId);
}