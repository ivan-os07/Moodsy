package ru.urfu.service;

import ru.urfu.model.PlatformIdentity;
import ru.urfu.model.User;
import ru.urfu.repository.UserRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Сервис поиска, создания и связывания пользователей с аккаунтами платформ
 */
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository, "userRepository не может быть null");
    }

    /**
     * Возвращает внутренний UUID пользователя или создает пользователя
     */
    public UUID getOrCreateUserId(PlatformIdentity identity){
        return userRepository.getOrCreate(identity).getId();
    }

    /**
     * Привязывает дополнительный аккаунт с платформы к существующему пользователю
     */
    public User LinkPlatform(UUID userId, PlatformIdentity identity){
        return userRepository.addPlatformIdentity(userId, identity);
    }
}
