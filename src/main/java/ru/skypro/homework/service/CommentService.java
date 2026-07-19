package ru.skypro.homework.service;

import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;

public interface CommentService {
    Comments getComments(Integer adId);

    Comment addComment(Integer adId, CreateOrUpdateComment commentDto, String authorEmail);

    void deleteComment(Integer adId, Integer commentId, String authorEmail);

    Comment updateComment(Integer adId, Integer commentId, CreateOrUpdateComment commentDto, String authorEmail);

    boolean isCommentOwner(Integer commentId, String email);
}
