package ru.urfu.service;

import ru.urfu.model.PlatformIdentity;
import ru.urfu.model.User;
import ru.urfu.repository.UserRepository;

import java.util.Objects;
import java.util.UUID;

/**
 * Сервис поиска, создания
 * и связывания пользователей с аккаунтами платформ
 */
public class UserService {
    /**
     * Репозиторий для хранения и поиска пользователей
     */
    private final UserRepository userRepository;

    /**
     * Создает сервис для поиска, создания и связывания пользователей
     * с аккаунтами платформ
     *
     * @param userRepository репозиторий для хранения и поиска пользователей
     */
    public UserService(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(
                userRepository, "userRepository не может быть null");
    }

    /**
     * Возвращает внутренний UUID пользователя по его внешнему аккаунту
     * Если пользователь не найден (т.е. не существует) создает нового
     *
     * @param identity аккаунт внешней платформы
     * @return внутренний UUID
     */
    public UUID getOrCreateUserId(PlatformIdentity identity) {

        return userRepository.getOrCreate(identity).getId();
    }

    /**
     * Привязывает дополнительный аккаунт с платформы к существующему пользователю
     *
     * @param userId   внутренний UUID пользователя, к которому добавляется новый аккаунт
     * @param identity аккаунт внешней платформы
     * @return пользователь с обновленным списком идентичностей
     */
    public User linkPlatform(UUID userId, PlatformIdentity identity) {
        return userRepository.addPlatformIdentity(userId, identity);
    }


    /**
     * Объединяет двух пользователей. Пользователь-владелец остается,
     * платформы и записи присоединяемого переходят к нему,
     * присоединяемый удаляется
     *
     * @param ownerUserId   UUID пользователя, который остается
     * @param joiningUserId UUID пользователя, который присоединяется и удаляется
     * @throws PlatformConflictException если у обоих есть одна и та же платформа
     */
//    public void mergeUsers(UUID ownerUserId, UUID joiningUserId) {
//        Objects.requireNonNull(ownerUserId, "ownerUserId не может быть null");
//        Objects.requireNonNull(joiningUserId, "joiningUserId не может быть null");
//        userRepository.merge(ownerUserId, joiningUserId);
//    }
}
