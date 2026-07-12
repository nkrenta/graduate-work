package ru.skypro.homework.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import ru.skypro.homework.dto.Register;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(source = "username", target = "email")
    UserEntity toEntity(Register register);

    @Mapping(source = "id", target = "id", qualifiedByName = "longToInt")
    User toDto(UserEntity entity);

    void updateEntityFromDto(UpdateUser updateUser, @MappingTarget UserEntity entity);

    @Named("longToInt")
    default Integer longToInt(Long value) {
        return value != null ? value.intValue() : null;
    }
}
