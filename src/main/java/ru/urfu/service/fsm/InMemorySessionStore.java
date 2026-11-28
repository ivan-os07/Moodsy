package ru.urfu.service.fsm;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Хранилище сессий в оперативной памяти.
 * Потокобезопасная коллекция позволяет вести диалоги разных пользователей параллельно.
 */
public class InMemorySessionStore implements SessionStore{
    /**
     * Открытые сессии диалогов, разделённые по идентификаторам пользователей.
     */
    private final ConcurrentHashMap<UUID, UserSession> sessions = new ConcurrentHashMap<>();


    @Override
    public void save(UserSession session) {
        Objects.requireNonNull(session, "session не может быть null");
        sessions.put(session.userId(), session);
    }


    @Override
    public Optional<UserSession> findById(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        return Optional.ofNullable(sessions.get(userId));
    }

    @Override
    public Optional<UserSession> remove(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        return Optional.ofNullable(sessions.remove(userId));
    }

    @Override
    public boolean hasSession(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        return sessions.containsKey(userId);
    }
}
