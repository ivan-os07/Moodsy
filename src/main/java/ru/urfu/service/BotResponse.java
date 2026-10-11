package ru.urfu.service;

/**
 * Объект ответа бота (кому отправить + текст сообщения).
 * p.s. Используется record тк это простой контейнер для хранения данных
 * И record автоматически генерирует конструктор, геттеры и
 * equals(), hashCode() и toString()
 *
 * @param userId идентификатор пользователя-получателя
 * @param text   текст сообщения
 */
public record BotResponse(String userId, String text) {
}
