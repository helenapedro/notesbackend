package com.notesbackend.controller;

import com.notesbackend.dto.CreateNoteDto;
import com.notesbackend.exception.ResourceNotFoundException;
import com.notesbackend.model.Note;
import com.notesbackend.model.User;
import com.notesbackend.service.NoteService;
import com.notesbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Note>> getNotesByDateRange(
            Authentication authentication,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Pageable pageable = PageRequest.of(page, size);
        Page<Note> notes = noteService.getNotesByUserAndDateRange(user.getUid(), startDate, endDate, pageable);
        return ResponseEntity.ok(notes);
    }

    @GetMapping(value = "/public", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Page<Note>> getPublicNotes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Note> notes = noteService.getPublicNotes(pageable);
        return ResponseEntity.ok(notes);
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Note>getNoteById(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Note> optionalNote = noteService.getNoteById(id, user.getUid());

        if (optionalNote.isEmpty() || (!optionalNote.get().isPublic() && !optionalNote.get().getUser().getEmail().equals(email))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        return ResponseEntity.ok(optionalNote.get());
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Note> createNote(Authentication authentication, @RequestBody CreateNoteDto createNoteDto) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Note createdNote = noteService.createNote(createNoteDto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdNote);
    }

    
    @PutMapping("/{noteId}/togglePrivacy")
    public ResponseEntity<Note> toggleNotePrivacy(Authentication authentication ,@PathVariable Long noteId) {
    	String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Note updateNote = noteService.togglePrivacy(noteId, user);
        
        return ResponseEntity.ok(updateNote);
    }

    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteNoteById(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        if (noteService.deleteNoteById(id, user)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
