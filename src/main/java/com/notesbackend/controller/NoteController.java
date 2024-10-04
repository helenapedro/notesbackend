package com.notesbackend.controller;

import com.notesbackend.dto.CreateNoteDto;
import com.notesbackend.model.Tag;
import com.notesbackend.dto.TagDto;
import com.notesbackend.exception.ResourceNotFoundException;
import com.notesbackend.exception.UnauthorizedException;
import com.notesbackend.model.Note;
import com.notesbackend.model.NoteTag;
import com.notesbackend.model.User;
import com.notesbackend.repository.NoteRepository;
import com.notesbackend.service.NoteService;
import com.notesbackend.service.UserService;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserService userService;
    
    @Autowired
    private NoteRepository noteRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(NoteController.class);
    

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
    
    private boolean isNoteAccessibleByUser(Note note, User user) {
        return note.isPublic() || note.getUser().getEmail().equals(user.getEmail());
    }

    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @Transactional
    public ResponseEntity<CreateNoteDto> getNoteById(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Note note = noteService.getNoteById(id, user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        if (!isNoteAccessibleByUser(note, user)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        CreateNoteDto noteDto = convertToDto(note);
        
        return ResponseEntity.ok(noteDto);
    }

    private CreateNoteDto convertToDto(Note note) {
        List<NoteTag> noteTags = note.getNoteTags(); 
        logger.debug("NoteTags: {}", noteTags); 

        List<TagDto> tagDtos = noteTags.stream()
                .map(noteTag -> {
                	logger.debug("NoteTag: {}", noteTag);
                    return convertToTagDto(noteTag.getTag());
                })
                .collect(Collectors.toList());

        return new CreateNoteDto(note.getNid(), note.getTitle(), note.getBody(), tagDtos);
    }

    private TagDto convertToTagDto(Tag tag) {
        return new TagDto(tag.getTid(), tag.getName());
    }
    
    private List<NoteTag> convertToTagDtos(List<Map<String, Object>> tagDtos) {
        return tagDtos.stream()
                .map(tagDto -> new NoteTag())
                .collect(Collectors.toList());
    }


	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Note> createNote(Authentication authentication, @Valid @RequestBody CreateNoteDto createNoteDto) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Note createdNote = noteService.createNote(createNoteDto, user);
        return ResponseEntity.created(URI.create("/api/notes/" + createdNote.getNid())).body(createdNote);
    }

    
    @PutMapping("/{noteId}/togglePrivacy")
    public ResponseEntity<String> toggleNotePrivacy(Authentication authentication ,@PathVariable Long noteId) {
    	String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Note updateNote = noteService.togglePrivacy(noteId, user);
        
        return ResponseEntity.ok("Privacy status updated to: " + updateNote.isPublic());
    }
    
    @PatchMapping("/{noteId}")
    public ResponseEntity<Note> patchNote(Authentication authentication, @PathVariable Long noteId, @RequestBody Map<String, Object> updates) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Note existingNote = noteService.getNoteById(noteId, user.getUid())
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));

        // Apply partial updates
        updates.forEach((key, value) -> {
            switch (key) {
                case "title":
                    existingNote.setTitle((String) value);
                    break;
                case "body":
                    existingNote.setBody((String) value);
                    break;
                case "tags":
                    if (value instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> tagDtos = (List<Map<String, Object>>) value;
                        existingNote.setNoteTags(convertToTagDtos(tagDtos));
                    } else {
                        throw new IllegalArgumentException("Expected a List for tags.");
                    }
                    break;
                default:
                    throw new IllegalArgumentException("Field '" + key + "' is not recognized.");
            }
        });

        Note updatedNote = noteService.updateNote(noteId, existingNote, user);
        return ResponseEntity.ok(updatedNote);
    }


    @DeleteMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> deleteNoteById(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found"));
        
        if (!note.getUser().getUid().equals(user.getUid())) {
            throw new UnauthorizedException("You are not authorized to delete this note");
        }

        noteRepository.delete(note);
        return ResponseEntity.noContent().build();
    }



}
