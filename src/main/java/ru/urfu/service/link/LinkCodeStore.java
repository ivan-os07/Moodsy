package ru.urfu.service.link;

import java.util.Optional;
import java.util.UUID;

public interface LinkCodeStore {
    /**
     * Сохранение кода по id пользователя
     *
     * @param code        код
     * @param ownerUserId id пользователя
     * @return true, если код сохранён, иначе false
     */
    boolean putIfAbsent(UUID code, UUID ownerUserId);

    /**
     * Поиск пользователя по коду
     *
     * @param code код
     * @return id пользователя, если нашли, иначе пустой Optional
     */
    Optional<UUID> find(UUID code);

    /**
     * Удаление кода.
     * @param code код
     * @return true, если код был
     */
    boolean remove(UUID code);
}
