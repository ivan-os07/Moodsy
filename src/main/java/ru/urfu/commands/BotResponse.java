package ru.urfu.commands;

import ru.urfu.application.InteractionType;

import java.util.UUID;

/**
 * Объект ответа бота (кому отправить + текст сообщения).
 * p.s. Используется record тк это простой контейнер для хранения данных
 * И record автоматически генерирует конструктор, геттеры и
 * equals(), hashCode() и toString()
 *
 * @param userId идентификатор пользователя-получателя
 * @param text   текст сообщения
 */
public record BotResponse(UUID userId, String text, InteractionType interactionType) {
    public BotResponse( UUID userId, String text) {
        this(userId, text, InteractionType.NONE);
    }

}
