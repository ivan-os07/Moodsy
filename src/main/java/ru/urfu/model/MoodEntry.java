package ru.urfu.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Неизменяемая запись об оценке настроения
 *
 * @param createdAt дата и время создания записи
 * @param score     оценка настроения от 1 до 10
 * @param sphere    выбранная сфера;
 * @param note      короткая заметка; пустая строка означает отсутствие заметки
 */
public record MoodEntry(LocalDateTime createdAt, int score, Sphere sphere, String note) {
    /**
     * Проверяет обязательные значения и допустимый диапазон оценки
     *
     * @throws NullPointerException     если дата или сфера не заданы
     * @throws IllegalArgumentException если оценка выходит за разрешенный диапазон (от 1 до 10)
     */
    public MoodEntry {
        Objects.requireNonNull(createdAt, "createdAt не может быть null");
        Objects.requireNonNull(sphere, "sphere не может быть null");
        if (score < 1 || score > 10) {
            throw new IllegalArgumentException("score должен быть в диапазоне от 1 до 10: " + score);
        }
        note = note == null ? "" : note;

    }
}
