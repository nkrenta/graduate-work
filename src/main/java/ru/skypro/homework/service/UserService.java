package ru.skypro.homework.service;

import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.entity.UserEntity;

import java.util.Optional;

/**
 * Сервис для работы с пользователями.
 * Управление профилем: получение, обновление данных, смена пароля и аватара.
 */
public interface UserService {

    /** Получить профиль пользователя по email. */
    User getUser(String email);

    /** Обновить имя и фамилию пользователя. */
    User updateUser(String email, UpdateUser updateUser);

    /** Обновить аватар пользователя. */
    User updateUserImage(String email, String imagePath);

    /** Сменить пароль (проверяет текущий пароль перед заменой). */
    void setPassword(String email, String currentPassword, String newPassword);

    /** Найти пользователя по email (возвращает Optional). */
    Optional<UserEntity> findByEmail(String email);

    /** Найти пользователя по email или выбросить исключение. */
    UserEntity getEntityByEmail(String email);
}
