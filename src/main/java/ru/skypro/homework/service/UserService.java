package ru.skypro.homework.service;

import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.entity.UserEntity;

import java.util.Optional;

public interface UserService {
    User getUser(String email);

    User updateUser(String email, UpdateUser updateUser);

    User updateUserImage(String email, String imagePath);

    Optional<UserEntity> findByEmail(String email);

    UserEntity getEntityByEmail(String email);
}
