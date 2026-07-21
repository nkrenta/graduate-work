package ru.skypro.homework.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;
import ru.skypro.homework.service.CommentService;

@RestController
@RequestMapping("/ads/{id}/comments")
@Tag(name = "Комментарии", description = "Управление комментариями к объявлениям")
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @Operation(summary = "Получение комментариев объявления", description = "Возвращает список комментариев к объявлению")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Comments.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Not found")
    @GetMapping
    public ResponseEntity<Comments> getComments(@PathVariable Integer id) {
        return ResponseEntity.ok(commentService.getComments(id));
    }

    @Operation(summary = "Добавление комментария к объявлению", description = "Создает новый комментарий")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Comment.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "404", description = "Not found")
    @PostMapping
    public ResponseEntity<Comment> addComment(
            @PathVariable Integer id,
            @RequestBody CreateOrUpdateComment comment,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.addComment(id, comment, authentication.getName()));
    }

    @Operation(summary = "Удаление комментария", description = "Удаляет комментарий по id")
    @ApiResponse(responseCode = "204", description = "No Content")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Not found")
    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Integer id,
            @PathVariable Integer commentId,
            Authentication authentication) {
        commentService.deleteComment(id, commentId, authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Обновление комментария", description = "Обновляет данные комментария")
    @ApiResponse(responseCode = "200", description = "OK",
            content = @Content(schema = @Schema(implementation = Comment.class)))
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @ApiResponse(responseCode = "403", description = "Forbidden")
    @ApiResponse(responseCode = "404", description = "Not found")
    @PatchMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Integer id,
            @PathVariable Integer commentId,
            @RequestBody CreateOrUpdateComment comment,
            Authentication authentication) {
        return ResponseEntity.ok(commentService.updateComment(id, commentId, comment, authentication.getName()));
    }
}
