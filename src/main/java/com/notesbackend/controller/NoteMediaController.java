package com.notesbackend.controller;

import com.notesbackend.exception.ResourceNotFoundException;
import com.notesbackend.model.NoteMedia;
import com.notesbackend.model.User;
import com.notesbackend.service.NoteMediaService;
import com.notesbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media")
public class NoteMediaController {

    @Autowired
    private NoteMediaService noteMediaService;

    @Autowired
    private UserService userService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NoteMedia> uploadMedia(Authentication authentication,
                                                  @RequestParam("file") MultipartFile file,
                                                  @RequestParam("noteId") Long noteId) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        NoteMedia media = noteMediaService.uploadMedia(file, noteId, user);
        return ResponseEntity.ok(media);
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteMedia> getMediaById(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
    	NoteMedia media = noteMediaService.getMediaById(id);
    	
    	// Check if the user owns the note or the note is public
        if (!media.getNote().getUser().getUid().equals(user.getUid()) && !media.getNote().isPublic()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    	
        return ResponseEntity.ok(media);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMediaById(Authentication authentication, @PathVariable Long id) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            noteMediaService.deleteMediaById(id, user);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

}
