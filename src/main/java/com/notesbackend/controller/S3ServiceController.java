package com.notesbackend.controller;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

public class S3ServiceController {

	public void uploadImage(Long noteId, MultipartFile file) {
	    try (InputStream inputStream = file.getInputStream()) {
	        String fileName = "images/" + noteId + "/" + file.getOriginalFilename();
	        String fileUrl = s3Service.uploadFile(fileName, inputStream, file.getSize());
	        // Save the fileUrl to the Image entity
	    } catch (IOException e) {
	        // Handle exceptions
	    }
	}

}
