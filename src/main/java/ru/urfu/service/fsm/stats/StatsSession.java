package ru.urfu.service.fsm.stats;

import ru.urfu.service.fsm.UserSession;

import java.util.Objects;
import java.util.UUID;

/**
 * Состояние диалога выбора периода статистики
 * @param userId внутренний идентификатор пользователя
 */
public record StatsSession(UUID userId)
        implements UserSession {
    /**
     * Проверяет обязательный идентификатор пользователя на null
     * @param userId внутренний идентификатор пользователя
     * @throws NullPointerException если {@code userId} равен {@code null}
     */
    public StatsSession {
        Objects.requireNonNull(userId, "userId не может быть null");
    }

}
