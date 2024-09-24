package com.notesbackend.service;

import com.notesbackend.exception.ResourceNotFoundException;
import com.notesbackend.model.Collaborator;
import com.notesbackend.model.Note;
import com.notesbackend.model.User;
import com.notesbackend.repository.CollaboratorRepository;
import com.notesbackend.repository.NoteRepository;
import com.notesbackend.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

// this service allows adding or removing collaborators with specific permissions
@Service
public class CollaboratorService {

    @Autowired
    private CollaboratorRepository collaboratorRepository;
    
    @Autowired
    private NoteRepository noteRepository;
    
    @Autowired
    private UserRepository userRepository;
    
//    public Collaborator addCollaborator(Note note, Long userId, String permissionLevel) {
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));
//
//        Collaborator collaborator = new Collaborator();
//        collaborator.setNote(note);
//        collaborator.setUser(user);
//        collaborator.setPermissionLevel(null);
//
//        return collaboratorRepository.save(collaborator);
//    }

    public Collaborator addCollaborator(Note noteId, Long userId, Collaborator.PermissionLevel permissionLevel) {
    	Note note = noteRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id " + noteId));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id " + userId));

        
        Collaborator collaborator = new Collaborator();
        collaborator.setNote(note);
        collaborator.setUser(user);
        collaborator.setPermissionLevel(permissionLevel);

        return collaboratorRepository.save(collaborator);
    }
    
    public void updatePermissionLevel(Long coll_id, Collaborator.PermissionLevel newPermissionLevel) {
        Collaborator collaborator = collaboratorRepository.findById(coll_id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with id " + coll_id));

        // Update the permission level
        collaborator.setPermissionLevel(newPermissionLevel);

        collaboratorRepository.save(collaborator);
    }
    
    public void removeCollaborator(Note note, User owner, Long collaboratorId) {
        if (!note.getUser().getUid().equals(owner.getUid())) {
            throw new AccessDeniedException("You are not authorized to remove collaborators from this note.");
        }
        
        collaboratorRepository.deleteByNoteAndUserUid(note, collaboratorId);
    }

    public List<Collaborator> getCollaboratorsForNote(Long noteId, User user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
        
        // Check if the user is the owner or a collaborator
        if (!note.getUser().getUid().equals(user.getUid()) && 
            !collaboratorRepository.existsByNoteAndUser(note, user)) {
            throw new AccessDeniedException("You are not authorized to view collaborators for this note.");
        }
        
        return collaboratorRepository.findByNote(note);
    }
}