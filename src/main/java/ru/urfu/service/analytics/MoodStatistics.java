package ru.urfu.service.analytics;

import ru.urfu.model.MoodEntry;
import ru.urfu.model.Sphere;

import java.util.Objects;

/**
 * Результат анализа заметок пользователя
 *
 * @param averageScore       средняя оценка настроения
 * @param bestDay            запись с наибольшей оценкой
 * @param worstDay           запись с наименьшей оценкой
 * @param mostFrequentSphere наиболее часто выбираемая сфера
 * @param sphereCount        количество записей в самой частой сфере
 */
public record MoodStatistics(
        double averageScore,
        MoodEntry bestDay,
        MoodEntry worstDay,
        Sphere mostFrequentSphere,
        int sphereCount
) {
    public MoodStatistics {
        Objects.requireNonNull(bestDay, "bestDay не может быть null");
        Objects.requireNonNull(worstDay, "worstDay не может быть null");
        Objects.requireNonNull(mostFrequentSphere, "mostFrequentSphere не может быть null");

        if (sphereCount <= 0) {
            throw new IllegalArgumentException("sphereCount должен быть натуральным");
        }
    }
}
