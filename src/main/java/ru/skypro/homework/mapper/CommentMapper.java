package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import ru.skypro.homework.dto.Comment;
import ru.skypro.homework.entity.CommentEntity;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(source = "id", target = "pk", qualifiedByName = "longToInt")
    @Mapping(source = "author.id", target = "author", qualifiedByName = "longToInt")
    @Mapping(source = "author.firstName", target = "authorFirstName")
    @Mapping(source = "author.image", target = "authorImage")
    Comment toDto(CommentEntity entity);

    @Named("longToInt")
    default Integer longToInt(Long value) {
        return value != null ? value.intValue() : null;
    }
}
