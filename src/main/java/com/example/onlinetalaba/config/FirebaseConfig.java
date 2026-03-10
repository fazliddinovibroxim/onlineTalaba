package com.example.onlinetalaba.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@Slf4j
public class FirebaseConfig {

    private final ResourceLoader resourceLoader;

    @Value("${app.firebase.config-path}")
    private String firebaseConfigPath;

    @Value("${app.firebase.fail-fast:false}")
    private boolean failFast;

    public FirebaseConfig(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void init() throws Exception {
        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        Resource resource = resolveFirebaseResource();

        if (!resource.exists()) {
            String message = "Firebase config file not found: " + firebaseConfigPath +
                    ". Set app.firebase.config-path or FIREBASE_CONFIG_PATH to a valid local file.";
            if (failFast) {
                throw new IllegalStateException(message);
            }

            log.warn("{} Firebase integration will be disabled until a valid credential file is provided.", message);
            return;
        }

        try (InputStream serviceAccount = resource.getInputStream()) {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            FirebaseApp.initializeApp(options);
            log.info("Firebase initialized successfully from {}", firebaseConfigPath);
        } catch (Exception e) {
            if (failFast) {
                throw e;
            }
            log.warn("Firebase could not be initialized. Firebase integration will be disabled. Reason: {}", e.getMessage());
        }
    }

    private Resource resolveFirebaseResource() {
        if (firebaseConfigPath == null || firebaseConfigPath.isBlank()) {
            throw new IllegalStateException("Firebase config path is empty");
        }

        String path = firebaseConfigPath.trim();

        if (path.startsWith("classpath:") || path.startsWith("file:")) {
            return resourceLoader.getResource(path);
        }

        Path fileSystemPath = Paths.get(path).toAbsolutePath().normalize();
        if (Files.exists(fileSystemPath)) {
            return resourceLoader.getResource("file:" + fileSystemPath);
        }

        return resourceLoader.getResource("classpath:" + path);
    }
}
