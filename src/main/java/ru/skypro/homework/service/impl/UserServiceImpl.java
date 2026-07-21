package ru.skypro.homework.service.impl;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.skypro.homework.dto.UpdateUser;
import ru.skypro.homework.dto.User;
import ru.skypro.homework.entity.UserEntity;
import ru.skypro.homework.mapper.UserMapper;
import ru.skypro.homework.repository.UserRepository;
import ru.skypro.homework.service.UserService;

import java.util.Optional;

/**
 * Реализация {@link UserService}.
 * Обновление профиля, аватара и пароля через UserRepository и UserMapper.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final String USER_NOT_FOUND = "User not found: ";

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User getUser(String email) {
        UserEntity entity = getEntityByEmail(email);
        return userMapper.toDto(entity);
    }

    @Override
    public User updateUser(String email, UpdateUser updateUser) {
        UserEntity entity = getEntityByEmail(email);
        userMapper.updateEntityFromDto(updateUser, entity);
        userRepository.save(entity);
        return userMapper.toDto(entity);
    }

    @Override
    public User updateUserImage(String email, String imagePath) {
        UserEntity entity = getEntityByEmail(email);
        entity.setImage(imagePath);
        userRepository.save(entity);
        return userMapper.toDto(entity);
    }

    @Override
    public void setPassword(String email, String currentPassword, String newPassword) {
        UserEntity entity = getEntityByEmail(email);
        if (!passwordEncoder.matches(currentPassword, entity.getPassword())) {
            throw new RuntimeException("Current password is incorrect");
        }
        entity.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(entity);
    }

    @Override
    public Optional<UserEntity> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public UserEntity getEntityByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND + email));
    }
}
