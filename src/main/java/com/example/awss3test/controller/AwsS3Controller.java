package com.example.awss3test.controller;

import com.example.awss3test.service.AwsS3Service;
import com.example.awss3test.utils.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class AwsS3Controller {
    @Autowired
    AwsS3Service awsS3Service;

    @GetMapping("/healthcheck")
    public String healthcheck() {
        return "OK";
    }

    @PostMapping("/upload")
    public String upload(@RequestParam("category") String category,
                         @RequestPart(value="file") MultipartFile file) throws IOException {
        return awsS3Service.uploadFileV1(category, file);
    }

    private HttpHeaders buildHeaders(String resourcePath, byte[] data) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentLength(data.length);
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDisposition(CommonUtils.createContentDisposition(resourcePath));
        return headers;
    }

    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam("resourcePath") String resourcePath) throws IOException {
        byte[] data = awsS3Service.downloadFileV1(resourcePath);
        ByteArrayResource resource = new ByteArrayResource(data);
        HttpHeaders headers = buildHeaders(resourcePath, data);
        return ResponseEntity.ok().headers(headers).body(resource);
    }
}
