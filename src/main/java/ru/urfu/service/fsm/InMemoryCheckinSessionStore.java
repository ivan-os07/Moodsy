package ru.urfu.service.fsm;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Хранилище сессий чекина в оперативной памяти.
 * Потокобезопасная коллекция позволяет вести диалоги разных пользователей параллельно.
 */
public class InMemoryCheckinSessionStore implements CheckinSessionStore {
    /**
     * Открытые сессии диалогов, разделённые по идентификаторам пользователей.
     */
    private final ConcurrentHashMap<UUID, CheckinSession> sessions = new ConcurrentHashMap<>();


    @Override
    public void save(CheckinSession session) {
        Objects.requireNonNull(session, "session не может быть null");
        sessions.put(session.userId(), session);
    }


    @Override
    public Optional<CheckinSession> findById(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        return Optional.ofNullable(sessions.get(userId));
    }

    @Override
    public Optional<CheckinSession> remove(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        return Optional.ofNullable(sessions.remove(userId));
    }

    @Override
    public boolean hasSession(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        return sessions.containsKey(userId);
    }
}
