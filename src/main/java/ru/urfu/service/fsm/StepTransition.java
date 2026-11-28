package ru.urfu.service.fsm;

import ru.urfu.application.BotResponse;

import java.util.Objects;
import java.util.Optional;

public record StepTransition(BotResponse response, Optional<UserSession> nextSession) {

    public StepTransition {
        Objects.requireNonNull(response, "response не может быть null");
        Objects.requireNonNull(nextSession, "nextSession не может быть null");
    }

    /**
     * Переход с сохранением новой или обновлённой сессии.
     */
    public StepTransition(BotResponse response, UserSession nextSession) {
        this(response, Optional.of(Objects.requireNonNull(nextSession, "nextSession не может быть null")));
    }

    /**
     * Завершение диалога (сессия будет удалена).
     */
    public StepTransition(BotResponse response) {
        this(response, Optional.empty());
    }
}