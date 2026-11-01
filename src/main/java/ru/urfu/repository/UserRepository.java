package ru.urfu.repository;

import ru.urfu.model.PlatformIdentity;
import ru.urfu.model.User;

import java.util.Optional;
import java.util.UUID;

/**
 * Контракт хранения пользователей и списка их привязанных платформ
 */
public interface UserRepository {
    /**
     * Ищет пользователя по внутреннему UUID
     */
    Optional<User> findById(UUID id);

    /**
     * Ищет пользователя по идентификатору аккаунта с платформы
     */
    Optional<User> findByPlatformIdentity(PlatformIdentity identity);
    /**
     * Возвращает пользователя по идентификатору аккаунта платформы
     * если такого нет, создает нового пользователя бота
     * Операция является атомарной
     */
    User getOrCreate(PlatformIdentity identity);

    /**
     * Привязывает к существующему пользователю дополнительный аккаунт другой платформы
     * @throws IllegalArgumentException если пользователь не существует
     * или у него уже привязан аккаунт с этой платформы
     *
     * @throws IllegalStateException если аккаунт с другой платформы
     * уже привязан у другого пользователя
     */
    User addPlatformIdentity(UUID userId, PlatformIdentity identity);
}
