package com.draxlmaier.leavehub.service;

import com.draxlmaier.leavehub.exception.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public String store(Long leaveRequestId, MultipartFile file) {
        try {
            Path dir = Paths.get(uploadDir, String.valueOf(leaveRequestId));
            Files.createDirectories(dir);

            String safeName = UUID.randomUUID() + "_" + sanitize(file.getOriginalFilename());
            Path target = dir.resolve(safeName);
            Files.copy(file.getInputStream(), target);

            return target.toString();
        } catch (IOException e) {
            throw new BusinessException("Nu s-a putut salva fisierul atasat: " + e.getMessage());
        }
    }

    public byte[] read(String filePath) {
        try {
            return Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            throw new BusinessException("Nu s-a putut citi fisierul atasat: " + e.getMessage());
        }
    }

    public void delete(String filePath) {
        try {
            Files.deleteIfExists(Paths.get(filePath));
        } catch (IOException ignored) {
            // best-effort
        }
    }

    private String sanitize(String name) {
        if (name == null) {
            return "fisier";
        }
        return name.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
