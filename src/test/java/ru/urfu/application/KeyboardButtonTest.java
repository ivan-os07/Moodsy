package ru.urfu.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Тестируем кнопки универсальной клавиатуры
 */
class KeyboardButtonTest {
    /**
     * Тестируем создание корректной кнопки
     */
    @Test
    void testCreateButton() {
        KeyboardButton button = new KeyboardButton("Неделя", "week");
        Assertions.assertEquals("Неделя", button.label());
        Assertions.assertEquals("week", button.action());
    }

    /**
     * Тестируем исключение при подписи равной null
     */
    @Test
    void testRejectsNullLabel() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new KeyboardButton(null, "week")
        );

        Assertions.assertEquals("label не равно null", exception.getMessage()
        );
    }
    /**
     * Тестируем исключение при действии равном null
     */
    @Test
    void testRejectsNullAction() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new KeyboardButton("Неделя", null)
        );

        Assertions.assertEquals("action не равно null", exception.getMessage()
        );
    }

    /**
     * Тестируем исключение при пустой подписи
     */
    @Test
    void testRejectsEmptyLabel() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new KeyboardButton("", "week")
        );

        Assertions.assertEquals("Подпись и действие кнопки не должны быть пустыми",
                exception.getMessage());
    }

    /**
     * Тестируем исключение при пустом действии
     */
    @Test
    void testRejectsEmptyAction() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new KeyboardButton("Неделя", "")
        );

        Assertions.assertEquals("Подпись и действие кнопки не должны быть пустыми",
                exception.getMessage());

    }
}
