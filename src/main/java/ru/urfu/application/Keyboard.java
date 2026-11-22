package ru.urfu.application;

import java.util.List;
import java.util.Objects;

public record Keyboard(List<KeyboardRow> rows) {
    public Keyboard {
        Objects.requireNonNull(rows, "rows не может быть null");
        rows = List.copyOf(rows);
    }

}
