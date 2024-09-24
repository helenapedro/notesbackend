package com.notesbackend.controller;

import com.notesbackend.model.NoteMedia;
import com.notesbackend.model.User;
import com.notesbackend.service.S3Service;
import com.notesbackend.service.UserService;

import java.nio.file.AccessDeniedException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/media")
public class S3ServiceController {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private UserService userService;

    @PostMapping("/upload")
    public ResponseEntity<NoteMedia> uploadMedia(Authentication authentication,
                                                  @RequestParam("file") MultipartFile file,
                                                  @RequestParam("noteId") Long noteId,
                                                  @RequestParam("isImage") boolean isImage) throws AccessDeniedException {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        NoteMedia media = s3Service.uploadMedia(file, noteId, user, isImage);
        return ResponseEntity.ok(media);
    }

    @DeleteMapping("/{mediaId}")
    public ResponseEntity<Void> deleteMediaById(Authentication authentication, @PathVariable Long mediaId) {
        String email = authentication.getName();
        User user = userService.getUserByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (s3Service.deleteMediaById(mediaId, user)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}