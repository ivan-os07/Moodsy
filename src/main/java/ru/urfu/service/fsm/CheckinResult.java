package ru.urfu.service.fsm;

import ru.urfu.application.InteractionType;

import java.util.Objects;

/**
 * Результат обработки шага диалога чекина:
 * текст ответа и тип клавиатуры
 *
 * @param text            текст ответа
 * @param interactionType тип клавиатуры
 */
public record CheckinResult(String text, InteractionType interactionType) {
    /**
     * Проверяет наличие текста и типа взаимодействия
     *
     * @throws NullPointerException если text или interactionType равны null
     */
    public CheckinResult {
        Objects.requireNonNull(interactionType, "interactionType не должен быть null");
        Objects.requireNonNull(text, "text не должен быть null");
    }
}


