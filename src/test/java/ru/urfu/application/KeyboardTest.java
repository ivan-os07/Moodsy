package ru.urfu.application;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Тестируем универсальную клавиатуру
 */
class KeyboardTest {
    /**
     * Тестируем исключение при null вместо списка строк
     */
    @Test
    void testRejectNullRows(){
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new Keyboard(null)
        );

        Assertions.assertEquals("rows не может быть null", exception.getMessage()
        );
    }

    /**
     * Тестируем, что исходный список не изменяет созданную клавиатуру
     */
    @Test
    void testCreatesDefensiveCopy() {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow(List.of(
                new KeyboardButton("Неделя", "week")
        )));

        Keyboard keyboard = new Keyboard(rows);
        rows.clear();
        Assertions.assertEquals(1, keyboard.rows().size());
    }
    /**
     * Тестируем неизменяемость списка строк
     */
    @Test
    void testRowsUnmodifiable() {
        Keyboard keyboard = new Keyboard(List.of(
                new KeyboardRow(List.of(
                        new KeyboardButton("Неделя", "week")))
        ));
        // стандартное поведение неизменяемой коллекции
        Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> keyboard.rows().clear()
        );
    }
}
