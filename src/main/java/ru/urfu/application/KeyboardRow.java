package ru.urfu.application;

import java.util.List;
import java.util.Objects;

/**
 * Строка универсальной клавиатуры
 *
 * @param buttons кнопки строки в порядке отображения
 */
public record KeyboardRow(List<KeyboardButton> buttons) {
    public KeyboardRow {
        Objects.requireNonNull(buttons, "buttons не может быть null");
        buttons = List.copyOf(buttons);
        if (buttons.isEmpty()) {
            throw new IllegalArgumentException("Строка клавиатуры должна содержать кнопки");
        }
    }

}
