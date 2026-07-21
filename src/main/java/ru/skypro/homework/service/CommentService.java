package ru.skypro.homework.service;

import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;

/**
 * Сервис для работы с комментариями к объявлениям.
 * CRUD-операции: получение списка, добавление, редактирование и удаление.
 */
public interface CommentService {

    /** Получить все комментарии к объявлению. */
    Comments getComments(Integer adId);

    /** Добавить комментарий к объявлению. */
    Comment addComment(Integer adId, CreateOrUpdateComment commentDto, String authorEmail);

    /** Удалить комментарий (только владелец). */
    void deleteComment(Integer adId, Integer commentId, String authorEmail);

    /** Обновить текст комментария (только владелец). */
    Comment updateComment(Integer adId, Integer commentId, CreateOrUpdateComment commentDto, String authorEmail);

    /** Проверить, является ли пользователь владельцем комментария. */
    boolean isCommentOwner(Integer commentId, String email);
}
