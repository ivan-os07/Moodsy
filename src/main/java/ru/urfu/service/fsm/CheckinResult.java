package ru.urfu.service.fsm;

import ru.urfu.application.Keyboard;

import java.util.Objects;
import java.util.Optional;

/**
 * Результат обработки шага диалога чекина:
 * текст ответа и тип клавиатуры
 *
 * @param text            текст ответа
 * @param keyboard
 */
public record CheckinResult(String text, Optional<Keyboard> keyboard) {
    /**
     * Проверяет наличие текста и типа взаимодействия
     *
     * @throws NullPointerException если text или interactionType равны null
     */
    public CheckinResult {
        Objects.requireNonNull(keyboard, "keyboard не должен быть null");
        Objects.requireNonNull(text, "text не должен быть null");
    }
}


