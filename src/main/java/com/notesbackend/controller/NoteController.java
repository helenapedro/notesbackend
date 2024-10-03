package com.notesbackend.controller;

import com.notesbackend.dto.CreateNoteDto;
import com.notesbackend.model.Tag;
import com.notesbackend.dto.TagDto;
import com.notesbackend.exception.ResourceNotFoundException;
import com.notesbackend.model.Note;
import com.notesbackend.model.NoteTag;
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
import java.util.List;
import java.util.stream.Collectors;

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
    public ResponseEntity<CreateNoteDto> getNoteById(
            Authentication authentication,
            @PathVariable Long id) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Note note = noteService.getNoteById(id, user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        if (!note.isPublic() && !note.getUser().getEmail().equals(email)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CreateNoteDto noteDto = convertToDto(note);
        return ResponseEntity.ok(noteDto);
    }

    private CreateNoteDto convertToDto(Note note) {
        List<NoteTag> noteTags = note.getNoteTags(); // Check the type and content here
        System.out.println("NoteTags: " + noteTags); // Debugging

        List<TagDto> tagDtos = noteTags.stream()
                .map(noteTag -> {
                    System.out.println("NoteTag: " + noteTag); // Debugging
                    return convertToTagDto(noteTag.getTag()); // Ensure noteTag.getTag() is returning a Tag
                })
                .collect(Collectors.toList());

        return new CreateNoteDto(
                note.getNid(),
                note.getTitle(),
                note.getBody(),
                tagDtos
        );
    }

    private TagDto convertToTagDto(Tag tag) {
        return new TagDto(tag.getTid(), tag.getName());
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
    
    @PutMapping("/{noteId}")
    public ResponseEntity<Note> updateNote(
            Authentication authentication,
            @PathVariable Long noteId,
            @RequestBody Note updatedNote) {
        
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Find the existing note by ID and verify ownership
        Note existingNote = noteService.getNoteById(noteId, user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        // Update the note's details
        existingNote.setTitle(updatedNote.getTitle());
        existingNote.setBody(updatedNote.getBody());

        // Save the updated note
        Note savedNote = noteService.updateNote(noteId, existingNote, user);
        
        return ResponseEntity.ok(savedNote);
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
