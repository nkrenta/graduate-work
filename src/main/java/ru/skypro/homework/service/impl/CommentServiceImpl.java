package ru.skypro.homework.service.impl;

import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.dto.Comments;
import ru.skypro.homework.dto.CreateOrUpdateComment;
import ru.skypro.homework.entity.AdEntity;
import ru.skypro.homework.entity.CommentEntity;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.mapper.CommentMapper;
import ru.skypro.homework.repository.AdRepository;
import ru.skypro.homework.repository.CommentRepository;
import ru.skypro.homework.service.CommentService;
import ru.skypro.homework.service.UserService;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {

    private static final String AD_NOT_FOUND = "Ad not found: ";
    private static final String COMMENT_NOT_FOUND = "Comment not found: ";
    private static final String NOT_AUTHORIZED_TO_UPDATE = "Not authorized to update this comment";
    private static final String NOT_AUTHORIZED_TO_DELETE = "Not authorized to delete this comment";

    private final CommentRepository commentRepository;
    private final AdRepository adRepository;
    private final CommentMapper commentMapper;
    private final UserService userService;

    public CommentServiceImpl(CommentRepository commentRepository,
                              AdRepository adRepository,
                              CommentMapper commentMapper,
                              UserService userService) {
        this.commentRepository = commentRepository;
        this.adRepository = adRepository;
        this.commentMapper = commentMapper;
        this.userService = userService;
    }

    @Override
    public Comments getComments(Integer adId) {
        List<CommentEntity> entities = commentRepository.findByAdIdOrderByCreatedAtDesc(adId.longValue());
        List<Comment> comments = entities.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());

        Comments result = new Comments();
        result.setCount(comments.size());
        result.setResults(comments);
        return result;
    }

    @Override
    public Comment addComment(Integer adId, CreateOrUpdateComment commentDto, String authorEmail) {
        AdEntity ad = adRepository.findById(adId.longValue())
                .orElseThrow(() -> new RuntimeException(AD_NOT_FOUND + adId));
        UserEntity author = userService.getEntityByEmail(authorEmail);

        CommentEntity entity = new CommentEntity();
        entity.setText(commentDto.getText());
        entity.setCreatedAt(System.currentTimeMillis());
        entity.setAd(ad);
        entity.setAuthor(author);

        CommentEntity saved = commentRepository.save(entity);
        return commentMapper.toDto(saved);
    }

    @Override
    public void deleteComment(Integer adId, Integer commentId, String authorEmail) {
        CommentEntity entity = commentRepository.findById(commentId.longValue())
                .orElseThrow(() -> new RuntimeException(COMMENT_NOT_FOUND + commentId));

        if (!entity.getAuthor().getEmail().equals(authorEmail)) {
            throw new RuntimeException(NOT_AUTHORIZED_TO_DELETE);
        }

        commentRepository.delete(entity);
    }

    @Override
    public Comment updateComment(Integer adId, Integer commentId, CreateOrUpdateComment commentDto, String authorEmail) {
        CommentEntity entity = commentRepository.findById(commentId.longValue())
                .orElseThrow(() -> new RuntimeException(COMMENT_NOT_FOUND + commentId));

        if (!entity.getAuthor().getEmail().equals(authorEmail)) {
            throw new RuntimeException(NOT_AUTHORIZED_TO_UPDATE);
        }

        entity.setText(commentDto.getText());
        CommentEntity saved = commentRepository.save(entity);
        return commentMapper.toDto(saved);
    }
}
