package ru.skypro.homework.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(value = "http://localhost:3000")
@RestController
public class ImageController {

    @GetMapping(value = "/images/{id}", produces = {
            MediaType.IMAGE_PNG_VALUE,
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_GIF_VALUE,
            "image/*"
    })
    public ResponseEntity<byte[]> getImage(@PathVariable String id) {
        return ResponseEntity.ok(new byte[0]);
    }
}
