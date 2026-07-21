package ru.skypro.homework.service;

import ru.skypro.homework.dto.Register;

/**
 * Сервис аутентификации и регистрации пользователей.
 */
public interface AuthService {

    /** Вход по логину и паролю. */
    boolean login(String userName, String password);

    /** Регистрация нового пользователя. */
    boolean register(Register register);
}
