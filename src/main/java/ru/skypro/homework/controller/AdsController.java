package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.skypro.homework.dto.Ad;
import ru.skypro.homework.dto.Ads;
import ru.skypro.homework.dto.CreateOrUpdateAd;
import ru.skypro.homework.dto.ExtendedAd;
import ru.skypro.homework.service.AdsService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/ads")
@Tag(name = "Объявления", description = "Управление объявлениями")
public class AdsController {

    private final AdsService adsService;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    public AdsController(AdsService adsService) {
        this.adsService = adsService;
    }

    @Operation(summary = "Получение всех объявлений", description = "Возвращает список всех объявлений")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Ads.class)))
    @GetMapping
    public ResponseEntity<Ads> getAllAds() {
        return ResponseEntity.ok(adsService.getAllAds());
    }

    @Operation(summary = "Добавление объявления", description = "Создает новое объявление")
    @ApiResponse(responseCode = "201", description = "Created",
            content = @Content(schema = @Schema(implementation = Ad.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> addAd(
            @RequestPart("properties") CreateOrUpdateAd properties,
            @RequestPart("image") MultipartFile image,
            Authentication authentication) throws IOException {
        String imagePath = saveImage(image, "ads");
        Ad ad = adsService.addAd(properties, imagePath, authentication.getName());
        return ResponseEntity.status(201).body(ad);
    }

    @Operation(summary = "Получение информации об объявлении", description = "Возвращает расширенную информацию об объявлении")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = ExtendedAd.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Not found")
    @GetMapping("/{id}")
    public ResponseEntity<ExtendedAd> getAds(@PathVariable Integer id) {
        return ResponseEntity.ok(adsService.getAd(id));
    }

    @Operation(summary = "Удаление объявления", description = "Удаляет объявление по id")
    @ApiResponse(responseCode = "204", description = "No Content")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Not found")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> removeAd(@PathVariable Integer id,
                                      Authentication authentication) {
        adsService.removeAd(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновление информации об объявлении", description = "Обновляет данные объявления")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Ad.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Not found")
    @PatchMapping("/{id}")
    public ResponseEntity<Ad> updateAds(
            @PathVariable Integer id,
            @RequestBody CreateOrUpdateAd updateAd,
            Authentication authentication) {
        return ResponseEntity.ok(adsService.updateAd(id, updateAd, authentication.getName()));
    }

    @Operation(summary = "Получение объявлений авторизованного пользователя", description = "Возвращает объявления текущего пользователя")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Ads.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/me")
    public ResponseEntity<Ads> getAdsMe(Authentication authentication) {
        return ResponseEntity.ok(adsService.getAdsMe(authentication.getName()));
    }

    @Operation(summary = "Обновление картинки объявления", description = "Обновляет изображение объявления")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Ad.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Not found")
    @PatchMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Ad> updateImage(
            @PathVariable Integer id,
            @RequestParam MultipartFile image,
            Authentication authentication) throws IOException {
        String imagePath = saveImage(image, "ads");
        return ResponseEntity.ok(adsService.updateImage(id, imagePath, authentication.getName()));
    }

    private String saveImage(MultipartFile image, String subfolder) throws IOException {
        validateImage(image);
        String originalFilename = image.getOriginalFilename();
        String extension = "";
        int dotIndex = originalFilename != null ? originalFilename.lastIndexOf('.') : -1;
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String uniqueFilename = UUID.randomUUID() + extension;

        Path dirPath = Path.of(uploadPath, subfolder);
        Files.createDirectories(dirPath);
        Path filePath = dirPath.resolve(uniqueFilename);
        Files.copy(image.getInputStream(), filePath);
        return "/images/" + subfolder + "/" + uniqueFilename;
    }

    private void validateImage(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.matches("image/(png|jpeg|gif)")) {
            throw new RuntimeException("Only PNG, JPEG, GIF images are allowed");
        }
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new RuntimeException("File size must not exceed 5 MB");
        }
    }
}
