package ru.urfu.commands;

import ru.urfu.application.InteractionType;

import java.util.UUID;

/**
 * Объект ответа бота (кому отправить + текст сообщения).
 * p.s. Используется record тк это простой контейнер для хранения данных
 * И record автоматически генерирует конструктор,
 * методы доступа к компонентам и
 * equals(), hashCode() и toString()
 *
 * @param userId          идентификатор пользователя-получателя
 * @param text            текст сообщения
 * @param interactionType тип взаимодействия, определяет клавиатуру ответа
 */
public record BotResponse(UUID userId, String text, InteractionType interactionType) {
    /**
     * Создает ответ без дополнительной клавиатуры
     *
     * @param userId идентификатор пользователя
     * @param text   текст сообщения
     */
    public BotResponse(UUID userId, String text) {
        this(userId, text, InteractionType.NONE);
    }

}
