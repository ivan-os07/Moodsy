package ru.urfu.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Тестируем строку универсальной клавиатуры
 */
class KeyboardRowTest {
    /**
     * Тестируем исключение при null вместо списка кнопок
     */
    @Test
    void testRejectsNullButtons() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new KeyboardRow(null)
        );

        Assertions.assertEquals("buttons не может быть null",
                exception.getMessage());
    }
    /**
     * Тестируем исключение при пустом списке кнопок
     */
    @Test
    void testRejectsEmptyButtons() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> new KeyboardRow(List.of())
        );
        Assertions.assertEquals("Строка клавиатуры должна содержать кнопки",
                exception.getMessage());
    }

    /**
     * Тестируем создание защищенной копии списка кнопок
     */
    @Test
    void testCreatesDefensiveCopy() {
        List<KeyboardButton> buttons = new ArrayList<>();
        buttons.add(new KeyboardButton("Неделя", "week"));
        KeyboardRow row = new KeyboardRow(buttons);
        buttons.clear();
        Assertions.assertEquals(1, row.buttons().size());
    }

    /**
     * Тестируем список кнопок на неизменяемость
     */
    @Test
    void testButtonsUnmodifiable() {
        KeyboardRow row = new KeyboardRow(List.of(
                new KeyboardButton("Неделя", "week")
        ));
        // стандартное поведение неизменяемой коллекции
        Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> row.buttons().clear()
        );
    }
}
