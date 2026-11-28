package ru.urfu.service.fsm;

import java.util.UUID;

/**
 * Объект сессии
 * userId идентификатор пользователя, которому принадлежит диалог.
 */
public interface UserSession {
    UUID userId();
}
