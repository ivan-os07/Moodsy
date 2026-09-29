package ru.urfu;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Модульные тесты для класса MyBot.
 */
class MyBotTest {
    /**
     * Экземпляр бота для тестирования.
     */
    MyBotLogic bot = new MyBotLogic();

    /**
     * Проверяет, что команда /start возвращает приветствие.
     */
    @Test
    void testStartCommand() {
        String expected = "Добро пожаловать!";
        String actual = bot.buildResponse("/start");

        assertEquals(expected, actual, "Метод должен возвращать `" + expected + "`");
    }
    /**
     * Проверяет, что команда /help возвращает справку.
     */
    @Test
    void testHelpCommand() {
        String expected = """
                    Привет! Я бот Moodsy
                    /start - начать работу
                    /help - показать справку
                    """;
        String actual = bot.buildResponse("/help");

        assertEquals(expected, actual, "Метод должен возвращать `" + expected + "`");

    }

    /**
     * Проверяет, корректность обработки echo функции.
     * -@ParameterizedTest для того, чтобы тест прошелся несколько раз
     * -@CsvSource работает как источник данных для теста
     */
    @ParameterizedTest
    @CsvSource({
            "привет,   Вы ввели привет",
            "123,      Вы ввели 123",
            "/unknown, Вы ввели /unknown"
    })
    void testEchoWithDifferentInputs(String input, String expected) {
        assertEquals(expected, bot.buildResponse(input));
    }
}