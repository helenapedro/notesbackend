package com.notesbackend.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import java.io.InputStream;

public class S3Service {
    private final AmazonS3 s3Client;
    private final String bucketName = "notesappmedia";

    public S3Service() {
        this.s3Client = AmazonS3ClientBuilder.defaultClient();
    }

    public String uploadFile(String keyName, InputStream inputStream, long length) {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(length);
        s3Client.putObject(bucketName, keyName, inputStream, metadata);
        return s3Client.getUrl(bucketName, keyName).toString(); // Returns the file URL
    }
}
