package ru.urfu.service;

import java.time.LocalDateTime;

/**
 * Неизменяемая запись об оценке настроения
 *
 * @param createdAt дата и время создания записи
 * @param score     оценка настроения от 1 до 10
 */
public record MoodEntry(LocalDateTime createdAt, int score) {

}
