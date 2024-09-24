package com.notesbackend.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;

import com.notesbackend.model.Note;
import com.notesbackend.model.NoteMedia;
import com.notesbackend.model.User;
import com.notesbackend.repository.NoteMediaRepository;
import com.notesbackend.repository.NoteRepository;
import com.notesbackend.exception.ResourceNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;

@Service
public class NoteMediaService {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private NoteMediaRepository noteMediaRepository;

    @Autowired
    private NoteRepository noteRepository;

    private final String bucketName = "notesappmedia";

    @PreAuthorize("principal.uid == #user.uid")
    public NoteMedia uploadMedia(MultipartFile file, Long noteId, User user) {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id " + noteId));

        // Check if the user owns the note
        if (!note.getUser().getUid().equals(user.getUid())) {
            throw new AccessDeniedException("You are not authorized to upload media for this note.");
        }
        
        try {
            InputStream inputStream = file.getInputStream();
            String fileName = file.getOriginalFilename();
            long contentLength = file.getSize();
            String fileType = file.getContentType();
            boolean isImage = fileType != null && fileType.startsWith("image/");

            // Upload file to S3
            String s3Url = uploadFileToS3(fileName, inputStream, contentLength);

            // Create NoteMedia entry
            NoteMedia media = new NoteMedia();
            media.setNote(note);
            media.setFileName(fileName);
            media.setFileType(fileType);
            media.setS3Url(s3Url);
            media.setUploadedBy(user);
            media.setImage(isImage);
            media.setFileSize(contentLength);
            media.setUploadedAt(LocalDateTime.now());

            return noteMediaRepository.save(media);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload media file", e);
        }
        
    }

    public NoteMedia getMediaById(Long mediaId) {
        return noteMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id " + mediaId));
    }

    public boolean deleteMediaById(Long mediaId, User user) {
        NoteMedia media = noteMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id " + mediaId));

        // Check if the user owns the note related to this media
        if (!media.getNote().getUser().getUid().equals(user.getUid())) {
            throw new AccessDeniedException("You are not authorized to delete this media.");
        }

        // Delete the file from S3
        deleteFileFromS3(media.getFileName());

        // Delete the media entry from the database
        noteMediaRepository.deleteById(mediaId);

        return true;
    }

    private String uploadFileToS3(String keyName, InputStream inputStream, long length) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(length);
        s3Client.putObject(bucketName, keyName, inputStream, metadata);
        return s3Client.getUrl(bucketName, keyName).toString(); // Return the S3 file URL
    }

    private void deleteFileFromS3(String fileName) {
    	try {
            s3Client.deleteObject(bucketName, fileName);
        } catch (AmazonServiceException e) {
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    	
    }
}

