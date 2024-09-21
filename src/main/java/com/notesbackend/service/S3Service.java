package com.notesbackend.service;

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

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.AccessDeniedException;

@Service
public class S3Service {

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private NoteMediaRepository noteMediaRepository;

    @Autowired
    private NoteRepository noteRepository;

    private final String bucketName = "notesappmedia";

    // Method to upload media (image/audio)
    public NoteMedia uploadMedia(MultipartFile file, Long noteId, User user, boolean isImage) throws AccessDeniedException {
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with id " + noteId));

        // Check if the user owns the note
        if (!note.getUser().getUid().equals(user.getUid())) {
            throw new AccessDeniedException("You are not authorized to upload media for this note.");
        }

        try (InputStream inputStream = file.getInputStream()) {
            String fileName = file.getOriginalFilename();
            long contentLength = file.getSize();
            String fileType = file.getContentType();

            // Upload file to S3
            String s3Url = uploadFileToS3(fileName, inputStream, contentLength, fileType);

            // Create NoteMedia entry
            NoteMedia media = new NoteMedia();
            media.setNote(note);
            media.setFileName(fileName);
            media.setFileType(fileType);
            media.setS3Url(s3Url);
            media.setFileSize(contentLength);
            media.setUploadedBy(user);
            media.setImage(isImage); // Set if it's an image or audio

            return noteMediaRepository.save(media);

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload media file", e);
        }
    }

    // Method to delete media
    public boolean deleteMediaById(Long mediaId, User user) {
        NoteMedia media = noteMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found with id " + mediaId));

        try {
            // Check if the user owns the note related to this media
            if (!media.getNote().getUser().getUid().equals(user.getUid())) {
                throw new AccessDeniedException("You are not authorized to delete this media.");
            }

            deleteFileFromS3(media.getFileName());

            // Delete the media entry from the database
            noteMediaRepository.deleteById(mediaId);

            return true;
        } catch (AccessDeniedException e) {
            // Handle the exception by logging or throwing a custom exception
            throw new RuntimeException("Access denied: " + e.getMessage());
        }
    }

    private String uploadFileToS3(String keyName, InputStream inputStream, long length, String contentType) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(length);
        metadata.setContentType(contentType); // Set the content type (image or audio)
        s3Client.putObject(bucketName, keyName, inputStream, metadata);
        return s3Client.getUrl(bucketName, keyName).toString(); // Return the S3 file URL
    }

    private void deleteFileFromS3(String fileName) {
        s3Client.deleteObject(bucketName, fileName);
    }
}
