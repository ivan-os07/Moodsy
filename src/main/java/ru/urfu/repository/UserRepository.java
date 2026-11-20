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
     *
     * @param id внутренний идентификатор пользователя
     * @return Optional с найденным пользователем или пустой Optional
     * @throws NullPointerException если id равен null
     */
    Optional<User> findById(UUID id);

    /**
     * Ищет пользователя по идентификатору аккаунта с платформы
     *
     * @param identity идентификатор пользователя с внешней платформы
     * @return Optional с найденным пользователем или пустой Optional
     * @throws NullPointerException если identity равен null
     */
    Optional<User> findByPlatformIdentity(PlatformIdentity identity);

    /**
     * Возвращает пользователя по идентификатору аккаунта платформы
     * если такого нет, создает нового пользователя бота
     *
     * @param identity идентификатор пользователя с внешней платформы
     * @return существующий или созданный пользователь
     * @throws NullPointerException если identity равен null
     */
    User getOrCreate(PlatformIdentity identity);

    /**
     * Привязывает к существующему пользователю дополнительный аккаунт другой платформы
     *
     * @param identity идентификатор пользователя с внешней платформы
     * @param userId   внутренний идентификатор пользователя
     * @throws NullPointerException     если identity или userId равен null
     * @throws IllegalArgumentException если пользователь не существует
     *                                  или у него уже привязан аккаунт с этой платформы
     * @throws IllegalStateException    если аккаунт с другой платформы
     *                                  уже привязан у другого пользователя
     */
    User addPlatformIdentity(UUID userId, PlatformIdentity identity);
}
