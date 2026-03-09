package com.example.onlinetalaba.service;

import com.example.onlinetalaba.repository.LogProgressRepository;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.HashSet;
import java.util.Set;

import static java.io.File.separator;
import static java.lang.System.currentTimeMillis;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {

    @Value("${custom_app_config.uploadDirectory}")
    private String fileUploadPath;

    @Value("${custom_app_config.baseUrl}")
    private String apiUrl;

    private final LogProgressRepository logProgressRepository;

    public String saveFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String userId,
            @Nonnull String fileUploadPath
    ) {
        final String fileUploadSubPath = fileUploadPath + separator + userId;
        return uploadFile(sourceFile, fileUploadSubPath);
    }

    private String uploadFile(
            @Nonnull MultipartFile sourceFile,
            @Nonnull String fileUploadSubPath
    ) {
        try {
            final String finalUploadPath = fileUploadPath + separator + fileUploadSubPath;
            File targetFolder = new File(finalUploadPath);

            String separator = FileSystems.getDefault().getSeparator();
            File classpath = ResourceUtils.getFile("classpath:");

            File generatedFileName = new File(classpath.getAbsolutePath().replaceAll("/", separator)
                    + separator + "ROOT" + separator
                    + "images" + separator + "folder" + separator + "logoName");

            if (!targetFolder.exists()) {
                boolean folderCreated = targetFolder.mkdirs();
                if (!folderCreated) {
                    log.error("Failed to create the target folder: {}", targetFolder);
                    return null;
                }
            }
            final String fileExtension = getFileExtension(sourceFile.getOriginalFilename());
            String targetFilePath = finalUploadPath + separator + currentTimeMillis() + "." + fileExtension;
            Path targetPath = Paths.get(targetFilePath);
            try {
                Files.write(targetPath, sourceFile.getBytes());
                // -r-xr--r--
                Set<PosixFilePermission> permissions = new HashSet<>();
                permissions.add(PosixFilePermission.OWNER_READ);
                permissions.add(PosixFilePermission.OWNER_EXECUTE);
                permissions.add(PosixFilePermission.GROUP_READ);
                permissions.add(PosixFilePermission.OTHERS_READ);

                try {
                    Files.setPosixFilePermissions(targetPath, permissions);
                    log.info("File permissions set to -r-xr--r-- for: {}", targetFilePath);
                } catch (IOException e) {
                    log.error("Failed to set file permissions: {}", e.getMessage());
                }

                return targetFilePath.replace("./", apiUrl + "/");
            } catch (IOException e) {
                log.error("File was not saved: {}", e.getMessage());
            }
            return null;
        } catch (Exception e) {
            log.error("uploadPhotoService: {}", e.getMessage());
            return null;
        }
    }

    private String getFileExtension(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return "";
        }
        int lastDotIndex = fileName.lastIndexOf(".");
        if (lastDotIndex == -1) {
            return "";
        }
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }
}
