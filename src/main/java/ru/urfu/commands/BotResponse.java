package ru.urfu.commands;

import ru.urfu.application.Keyboard;

import java.util.Optional;
import java.util.UUID;

/**
 * Объект ответа бота (кому отправить + текст сообщения).
 * p.s. Используется record тк это простой контейнер для хранения данных
 * И record автоматически генерирует конструктор, геттеры и
 * equals(), hashCode() и toString()
 *
 * @param userId   идентификатор пользователя-получателя
 * @param text     текст сообщения
 * @param keyboard определяет клавиатуру ответа
 */
public record BotResponse(UUID userId, String text, Optional<Keyboard> keyboard) {
    /**
     * Создает ответ без дополнительной клавиатуры
     *
     * @param userId идентификатор пользователя
     * @param text   текст сообщения
     */
    public BotResponse(UUID userId, String text) {
        this(userId, text, Optional.empty());
    }

}
