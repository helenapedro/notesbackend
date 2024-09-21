package com.notesbackend.controller;

import com.notesbackend.model.Collaborator;
import com.notesbackend.model.Note;
import com.notesbackend.model.User;
import com.notesbackend.service.CollaboratorService;
import com.notesbackend.service.NoteService;
import com.notesbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/notes/{noteId}/collaborators")
public class CollaboratorController {

    @Autowired
    private CollaboratorService collaboratorService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    // Add a collaborator to a note
    @PostMapping("/{userId}")
    public ResponseEntity<Collaborator> addCollaborator(
            @PathVariable Long noteId,
            @PathVariable Long userId,
            Authentication authentication) {
        
        String email = authentication.getName();
        User owner = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if the user is the owner of the note
        Note note = noteService.getNoteById(noteId, owner.getUid())
                .orElseThrow(() -> new RuntimeException("Note not found"));
        
        // Fetch the collaborator
        User collaboratorUser = userService.getUserById(userId)
                .orElseThrow(() -> new RuntimeException("Collaborator not found"));
        
        // Add the collaborator with PermissionLevel.READ
        Collaborator collaborator = collaboratorService.addCollaborator(note, collaboratorUser.getUid(), Collaborator.PermissionLevel.READ);
        
        return ResponseEntity.ok(collaborator);
    }



    // Remove a collaborator from a note
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> removeCollaborator(
            @PathVariable Long noteId,
            @PathVariable Long userId,
            Authentication authentication) {
        
        String email = authentication.getName();
        User owner = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Ensure the user removing the collaborator is the owner of the note
        Note note = noteService.getNoteById(noteId, owner.getUid())
                .orElseThrow(() -> new RuntimeException("Note not found"));
        
        collaboratorService.removeCollaborator(note, owner, userId);
        return ResponseEntity.ok().build();
    }


    // Get all collaborators for a note
    @GetMapping
    public ResponseEntity<List<Collaborator>> getCollaborators(@PathVariable Long noteId, Authentication authentication) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Collaborator> collaborators = collaboratorService.getCollaboratorsForNote(noteId, user);
        return ResponseEntity.ok(collaborators);
    }
}
