package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@Tag(name = "Изображения", description = "Получение изображений")
public class ImageController {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Operation(summary = "Получение изображения", description = "Возвращает изображение по пути")
    @ApiResponse(responseCode = "200", description = "OK")
    @GetMapping(value = "/images/{type}/{filename}", produces = {
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            "image/*"
    })
    public ResponseEntity<byte[]> getImage(@PathVariable String type,
                                           @PathVariable String filename) {
        try {
            Path filePath = Path.of(uploadPath, type, filename);
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }
            byte[] imageBytes = Files.readAllBytes(filePath);
            String contentType = detectContentType(filename);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    private String detectContentType(String filename) {
        if (filename.endsWith(".png")) return MediaType.IMAGE_PNG_VALUE;
        if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) return MediaType.IMAGE_JPEG_VALUE;
        if (filename.endsWith(".gif")) return MediaType.IMAGE_GIF_VALUE;
        return "image/*";
    }
}
