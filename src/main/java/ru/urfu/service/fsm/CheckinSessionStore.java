package ru.urfu.service.fsm;

import java.util.Optional;
import java.util.UUID;

/**
 * Контракт сущности, которая хранит сессии всех пользователей.
 * Позволяет нескольким пользователям проходить чекин одновременно.
 * Сессии не сохраняются между перезапусками бота.
 */
public interface CheckinSessionStore {

    /**
     * Сохраняет сессию пользователя, заменяя предыдущую сессию этого же пользователя.
     *
     * @param session сессия диалога
     */
    void save(CheckinSession session);

    /**
     * Возвращает открытую сессию пользователя.
     *
     * @param userId идентификатор пользователя
     * @return сессия пользователя или пустой Optional, если диалог не запущен
     */
    Optional<CheckinSession> findById(UUID userId);

    /**
     * Удаляет открытую сессию пользователя.
     *
     * @param userId идентификатор пользователя
     * @return удалённая сессия или пустой Optional, если удалять было нечего
     */
    Optional<CheckinSession> remove(UUID userId);

    /**
     * Проверяет есть сессия у пользователя
     *
     * @param userId идентификатор пользователя
     * @return есть ли сессия
     */
    boolean hasSession(UUID userId);
}
