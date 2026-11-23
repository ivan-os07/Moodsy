package ru.urfu.application;

import java.util.List;
import java.util.Objects;

/**
 * Универсальное описание клавиатуры, независимое от платформы
 * @param rows строки клавиатуры в порядке отображения
 */
public record Keyboard(List<KeyboardRow> rows) {
    /**
     * Проверяет список строк и создает его неизменяемую копию
     * @param rows строки клавиатуры
     * @throws NullPointerException если rows равен null
     */
    public Keyboard {
        Objects.requireNonNull(rows, "rows не может быть null");
        rows = List.copyOf(rows);
    }

}
