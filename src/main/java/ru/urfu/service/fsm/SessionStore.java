package ru.urfu.service.fsm;

import java.util.Optional;
import java.util.UUID;

/**
 * Контракт сущности, которая хранит сессии всех пользователей.
 * Позволяет нескольким пользователям проходить какой-либо сценарий FSM одновременно.
 * Сессии не сохраняются между перезапусками бота.
 */
public interface SessionStore {
    /**
     * Сохраняет сессию пользователя, заменяя предыдущую сессию этого же пользователя.
     *
     * @param session сессия диалога
     * @throws NullPointerException если session равен null
     */
    void save(UserSession session);

    /**
     * Возвращает открытую сессию пользователя.
     *
     * @param userId идентификатор пользователя
     * @return сессия пользователя или пустой Optional, если диалог не запущен
     * @throws NullPointerException если userId равен null
     */
    Optional<UserSession> findById(UUID userId);

    /**
     * Удаляет открытую сессию пользователя.
     *
     * @param userId идентификатор пользователя
     * @return удалённая сессия или пустой Optional, если удалять было нечего
     * @throws NullPointerException если userId равен null
     */
    Optional<UserSession> remove(UUID userId);

    /**
     * Проверяет есть сессия у пользователя
     *
     * @param userId идентификатор пользователя
     * @return true, если сессия существует, иначе false
     * @throws NullPointerException если userId равен null
     */
    boolean hasSession(UUID userId);
}
