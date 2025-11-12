package com.platoons.e_commerce.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface IS3Service {
    String uploadFile(MultipartFile file) throws IOException;
    void deleteFile(String fileName);
    String getFileUrl(String fileName);
}
