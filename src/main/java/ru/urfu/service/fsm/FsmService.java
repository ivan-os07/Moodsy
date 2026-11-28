package ru.urfu.service.fsm;

import java.util.UUID;

/**
 * Контракт отдельного диалогового сценария.
 *
 * @param <S> конкретный тип сессии, с которым работает сценарий
 */
public interface FsmService<S extends UserSession> {

    /**
     * Запускает новый диалог и возвращает первый шаг.
     */
    StepTransition start(UUID userId);

    /**
     * Обрабатывает ввод пользователя в рамках его текущей сессии.
     */
    StepTransition handle(S session, String input);
}