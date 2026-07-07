package ru.skypro.homework.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;

@CrossOrigin(value = "http://localhost:3000")
@RestController
@RequestMapping("/ads/{id}/comments")
public class CommentController {

    @GetMapping
    public ResponseEntity<Comments> getComments(@PathVariable Integer id) {
        Comments comments = new Comments();
        return ResponseEntity.ok(comments);
    }

    @PostMapping
    public ResponseEntity<Comment> addComment(
            @PathVariable Integer id,
            @RequestBody CreateOrUpdateComment comment,
            Authentication authentication) {
        Comment result = new Comment();
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Integer id,
            @PathVariable Integer commentId,
            Authentication authentication) {
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{commentId}")
    public ResponseEntity<Comment> updateComment(
            @PathVariable Integer id,
            @PathVariable Integer commentId,
            @RequestBody CreateOrUpdateComment comment,
            Authentication authentication) {
        Comment result = new Comment();
        return ResponseEntity.ok(result);
    }
}
