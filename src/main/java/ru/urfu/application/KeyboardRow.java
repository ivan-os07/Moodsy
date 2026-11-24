package ru.urfu.application;

import java.util.List;
import java.util.Objects;

/**
 * Строка универсальной клавиатуры
 *
 * @param buttons кнопки строки в порядке отображения
 */
public record KeyboardRow(List<KeyboardButton> buttons) {
    /**
     * Проверяет наличие кнопок и создаёт неизменяемую копию списка
     * @param buttons кнопки строки
     * @throws NullPointerException если buttons равен null
     * @throws IllegalArgumentException если строка не содержит кнопок (пустая)
     */
    public KeyboardRow {
        Objects.requireNonNull(buttons, "buttons не может быть null");
        buttons = List.copyOf(buttons);
        if (buttons.isEmpty()) {
            throw new IllegalArgumentException("Строка клавиатуры должна содержать кнопки");
        }
    }

}
