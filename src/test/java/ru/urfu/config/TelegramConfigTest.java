package ru.urfu.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тестируем класс конфигурации бота в телеграмме:
 * проверяем валидность секретного токена.
 */
class TelegramConfigTest {

    /**
     * Проверка на соответствие токена.
     * При передаче пары ключ-значения при инициализации,
     * конфиг устанавливает это значение в качестве ключа
     */
    @Test
    void getBotToken_withValidToken() {
        TelegramConfig config = new TelegramConfig(_ -> "valid_secret_token");

        assertEquals("valid_secret_token", config.getBotToken());
    }

    /**
     * Проверяет, что конструктор выбрасывает {@link IllegalStateException},
     * если переменная окружения с токеном отсутствует (равна null).
     */
    @Test
    void getBotToken_withInvalidToken() {
        assertThrows(IllegalStateException.class, () -> new TelegramConfig(_ -> null));
    }

    /**
     * Проверяет, что конструктор выбрасывает {@link IllegalStateException},
     * если переменная окружения задана, но состоит только из пробелов (пустая строка).
     */
    @Test
    void getBotToken_withBlankToken() {
        assertThrows(IllegalStateException.class, () -> new TelegramConfig(_ -> "   "));
    }

}