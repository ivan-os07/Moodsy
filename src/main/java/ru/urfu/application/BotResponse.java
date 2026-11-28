package ru.urfu.application;

import java.util.Objects;
import java.util.Optional;

/**
 * Единый ответ ядра бота для транспортного слоя:
 * текст сообщения и опциональная клавиатура.
 *
 * @param text     текст ответа пользователю
 * @param keyboard клавиатура для отображения, если она нужна
 */
public record BotResponse(String text, Optional<Keyboard> keyboard) {

    /**
     * Проверяет обязательные поля ответа.
     *
     * @throws NullPointerException если {@code text} или {@code keyboard} равны {@code null}
     */
    public BotResponse {
        Objects.requireNonNull(text, "text не может быть null");
        Objects.requireNonNull(keyboard, "keyboard не может быть null");
    }

    /**
     * Создаёт текстовый ответ без клавиатуры.
     *
     * @param text текст ответа пользователю
     * @throws NullPointerException если {@code text} равен {@code null}
     */
    public BotResponse(String text) {
        this(text, Optional.empty());
    }

    /**
     * Создаёт ответ с текстом и клавиатурой.
     *
     * @param text     текст ответа пользователю
     * @param keyboard клавиатура для отображения
     * @throws NullPointerException если {@code text} или {@code keyboard} равны {@code null}
     */
    public BotResponse(String text, Keyboard keyboard) {
        this(text, Optional.of(Objects.requireNonNull(keyboard, "keyboard не может быть null")));
    }
}