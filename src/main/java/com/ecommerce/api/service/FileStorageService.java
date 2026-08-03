package com.ecommerce.api.service;

import com.ecommerce.api.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

/**
 * Guarda las fotos de productos en disco (carpeta "uploads/" fuera del JAR)
 * y devuelve la URL pública con la que se sirven ("/uploads/archivo.jpg").
 * No usa ningún servicio externo: sirve tal cual para correr local o con Docker.
 */
@Service
@RequiredArgsConstructor
public class FileStorageService {

    private static final List<String> ALLOWED_TYPES =
            List.of("image/jpeg", "image/png", "image/webp", "image/gif");
    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024; // 5 MB

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadDir));
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear la carpeta de uploads: " + uploadDir, e);
        }
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("No se recibió ningún archivo");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new BusinessException("La imagen no puede pesar más de 5 MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_TYPES.contains(contentType)) {
            throw new BusinessException("Formato no permitido. Usá JPG, PNG, WEBP o GIF.");
        }

        String original = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
        String extension = original.contains(".") ? original.substring(original.lastIndexOf('.')) : "";
        String filename = UUID.randomUUID() + extension;

        try {
            Path target = Paths.get(uploadDir).resolve(filename).normalize();
            Files.copy(file.getInputStream(), target);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo guardar la imagen", e);
        }

        return "/uploads/" + filename;
    }
}
