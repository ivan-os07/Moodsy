package ru.urfu.service.fsm;

import ru.urfu.application.BotResponse;

import java.util.Objects;
import java.util.Optional;

/**
 * Результат обработки одного шага диалога в конечном автомате:
 * ответ пользователю и следующее состояние сессии.
 *
 * @param response    ответ бота для пользователя
 * @param nextSession новое состояние сессии или пустой {@link Optional}, если диалог завершён
 */
public record StepTransition(BotResponse response, Optional<UserSession> nextSession) {

    /**
     * Проверяет, что ответ и контейнер следующей сессии не равны {@code null}.
     *
     * @throws NullPointerException если {@code response} или {@code nextSession} равны {@code null}
     */
    public StepTransition {
        Objects.requireNonNull(response, "response не может быть null");
        Objects.requireNonNull(nextSession, "nextSession не может быть null");
    }

    /**
     * Создаёт переход с продолжением диалога и сохранением обновлённой сессии.
     *
     * @param response    ответ бота для пользователя
     * @param nextSession новое состояние сессии
     * @throws NullPointerException если {@code response} или {@code nextSession} равны {@code null}
     */
    public StepTransition(BotResponse response, UserSession nextSession) {
        this(response, Optional.of(Objects.requireNonNull(nextSession, "nextSession не может быть null")));
    }

    /**
     * Создаёт завершающий переход без следующей сессии (текущая сессия будет удалена).
     *
     * @param response ответ бота для пользователя
     * @throws NullPointerException если {@code response} равен {@code null}
     */
    public StepTransition(BotResponse response) {
        this(response, Optional.empty());
    }
}