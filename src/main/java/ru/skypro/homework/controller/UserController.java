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
import ru.skypro.homework.dto.NewPassword;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@Tag(name = "Пользователи", description = "Управление пользователями")
public class UserController {

    private final UserService userService;

    @Value("${upload.path:uploads}")
    private String uploadPath;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Обновление пароля", description = "Обновляет пароль авторизованного пользователя")
    @ApiResponse(responseCode = "200", description = "OK")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @PostMapping("/set_password")
    public ResponseEntity<?> setPassword(@RequestBody NewPassword newPassword,
                                         Authentication authentication) {
        userService.setPassword(authentication.getName(),
                newPassword.getCurrentPassword(), newPassword.getNewPassword());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Получение информации об авторизованном пользователе", description = "Возвращает данные текущего пользователя")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @GetMapping("/me")
    public ResponseEntity<User> getUser(Authentication authentication) {
        return ResponseEntity.ok(userService.getUser(authentication.getName()));
    }

    @Operation(summary = "Обновление информации об авторизованном пользователе", description = "Обновляет данные текущего пользователя")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = UpdateUser.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PatchMapping("/me")
    public ResponseEntity<UpdateUser> updateUser(@RequestBody UpdateUser updateUser,
                                                 Authentication authentication) {
        userService.updateUser(authentication.getName(), updateUser);
        return ResponseEntity.ok(updateUser);
    }

    @Operation(summary = "Обновление аватара авторизованного пользователя", description = "Обновляет изображение профиля")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = User.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @PatchMapping(value = "/me/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<User> updateUserImage(@RequestParam MultipartFile image,
                                                Authentication authentication) throws IOException {
        validateImage(image);

        // Удаление старого аватара
        User currentUser = userService.getUser(authentication.getName());
        if (currentUser.getImage() != null) {
            Path oldPath = Path.of(uploadPath, currentUser.getImage().replaceFirst("^/images/", ""));
            Files.deleteIfExists(oldPath);
        }

        String originalFilename = image.getOriginalFilename();
        String extension = "";
        int dotIndex = originalFilename != null ? originalFilename.lastIndexOf('.') : -1;
        if (dotIndex > 0) {
            extension = originalFilename.substring(dotIndex);
        }
        String uniqueFilename = UUID.randomUUID() + extension;

        Path dirPath = Path.of(uploadPath, "users");
        Files.createDirectories(dirPath);
        Path filePath = dirPath.resolve(uniqueFilename);
        Files.copy(image.getInputStream(), filePath);
        String imagePath = "/images/users/" + uniqueFilename;
        return ResponseEntity.ok(userService.updateUserImage(authentication.getName(), imagePath));
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
