package ru.urfu.config;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions;

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
    void getBotToken_withValidTokenTest() {
        TelegramConfig config = new TelegramConfig(_ -> "valid_secret_token");

        Assertions.assertEquals("valid_secret_token", config.getBotToken());
    }

    /**
     * Проверяет, что конструктор выбрасывает {@link IllegalStateException},
     * если переменная окружения с токеном отсутствует (равна null).
     */
    @Test
    void constructor_nullToken_throwsIllegalStateExceptionTest() {
        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> new TelegramConfig(_ -> null));

        Assertions.assertEquals(
                "Ошибка: переменная окружения BOT_TOKEN не задана!",
                exception.getMessage());
    }

    /**
     * Проверяет, что конструктор выбрасывает {@link IllegalStateException},
     * если переменная окружения задана, но состоит только из пробелов (пустая строка).
     */
    @Test
    void constructor_blankToken_throwsIllegalStateExceptionTest() {
        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> new TelegramConfig(_ -> "     "));

        Assertions.assertEquals(
                "Ошибка: переменная окружения BOT_TOKEN не задана!",
                exception.getMessage());
    }

}