package com.notesbackend.service;

import com.notesbackend.repository.NoteRepository;
import com.notesbackend.dto.CreateNoteDto;
import com.notesbackend.exception.ResourceNotFoundException;
import com.notesbackend.model.Note;
import com.notesbackend.model.User;
import com.notesbackend.model.NoteMedia;
import com.notesbackend.model.NoteTag;
import com.notesbackend.model.Collaborator;
import com.notesbackend.model.Comment;
import com.notesbackend.model.Favorite;
import com.notesbackend.repository.TagRepository;
import com.notesbackend.repository.NoteMediaRepository;
import com.notesbackend.repository.CollaboratorRepository;
import com.notesbackend.repository.CommentRepository;
import com.notesbackend.repository.FavoriteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class NoteService {

    private static final Logger logger = LoggerFactory.getLogger(NoteService.class);

    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private TagRepository tagRepository;

    @Autowired
    private NoteMediaRepository noteMediaRepository;

    @Autowired
    private CollaboratorRepository collaboratorRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

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
    public Optional<Note> getNoteById(Long id, Long userId) {
        return noteRepository.findById(id)
            .filter(foundNote -> foundNote.getUser().getUid().equals(userId));
    }

    public Note createNote(CreateNoteDto createNoteDto, User user) {
        Note note = new Note();
        note.setTitle(createNoteDto.getTitle());
        note.setBody(createNoteDto.getBody());
        note.setUser(user);
        return noteRepository.save(note);
    }


    @CachePut(value = "notes", key = "#user.uid + '-' + #id")
    public Note updateNote(Long id, Note updatedNote, User user) {
        Note note = noteRepository.findById(id).orElseThrow(() -> 
            new ResourceNotFoundException("Note not found with id " + id));
        
        if (note.getUser().getUid().equals(user.getUid())) {
            note.setTitle(updatedNote.getTitle());
            note.setBody(updatedNote.getBody());
            note.setUpdatedAt(LocalDateTime.now());

            // Handle tags, collaborators, media, comments, favorites as needed
            note.getNoteTags().clear();
            note.getNoteTags().addAll(updatedNote.getNoteTags());

            note.getCollaborators().clear();
            note.getCollaborators().addAll(updatedNote.getCollaborators());

            note.getMedia().clear();
            note.getMedia().addAll(updatedNote.getMedia());

            note.getComments().clear();
            note.getComments().addAll(updatedNote.getComments());

            note.getFavorites().clear();
            note.getFavorites().addAll(updatedNote.getFavorites());

            return noteRepository.save(note);
        } else {
            throw new AccessDeniedException("You are not authorized to update this note.");
        }
    }

    @CacheEvict(value = "notes", key = "#userId + '-' + #id")
    public boolean deleteNoteById(Long id, User user) {
        Note note = noteRepository.findById(id).orElse(null);
        if (note != null && note.getUser().getUid().equals(user.getUid())) {
            // Remove associated tags, media, collaborators, comments, and favorites before deletion
            tagRepository.deleteAll(note.getNoteTags().stream().map(NoteTag::getTag).toList());
            noteMediaRepository.deleteAll(note.getMedia());
            collaboratorRepository.deleteAll(note.getCollaborators());
            commentRepository.deleteAll(note.getComments());
            favoriteRepository.deleteAll(note.getFavorites());

            noteRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public Note togglePrivacy(Long noteId, User user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id " + noteId));

        if (!note.getUser().getUid().equals(user.getUid())) {
            throw new AccessDeniedException("You are not authorized to change the privacy of this note.");
        }

        logger.info("User {} is toggling the privacy of note {} from {} to {}",
                user.getEmail(), noteId, note.isPublic(), !note.isPublic());

        note.setPublic(!note.isPublic());
        return noteRepository.save(note);
    }

    // Additional helper methods to add/remove tags, collaborators, media, comments, favorites
    
    public Note addTagToNote(Note note, NoteTag tag) {
        note.getNoteTags().add(tag);
        return noteRepository.save(note);
    }

    public Note removeTagFromNote(Note note, NoteTag tag) {
        note.getNoteTags().remove(tag);
        return noteRepository.save(note);
    }

    public Note addCollaboratorToNote(Note note, Collaborator collaborator) {
        note.getCollaborators().add(collaborator);
        return noteRepository.save(note);
    }

    public Note removeCollaboratorFromNote(Note note, Collaborator collaborator) {
        note.getCollaborators().remove(collaborator);
        return noteRepository.save(note);
    }

    public Note addMediaToNote(Note note, NoteMedia media) {
        note.getMedia().add(media);
        return noteRepository.save(note);
    }

    public Note removeMediaFromNote(Note note, NoteMedia media) {
        note.getMedia().remove(media);
        return noteRepository.save(note);
    }

    public Note addCommentToNote(Note note, Comment comment) {
        note.getComments().add(comment);
        return noteRepository.save(note);
    }

    public Note removeCommentFromNote(Note note, Comment comment) {
        note.getComments().remove(comment);
        return noteRepository.save(note);
    }

    public Note addFavoriteToNote(Note note, Favorite favorite) {
        note.getFavorites().add(favorite);
        return noteRepository.save(note);
    }

    public Note removeFavoriteFromNote(Note note, Favorite favorite) {
        note.getFavorites().remove(favorite);
        return noteRepository.save(note);
    }
}
