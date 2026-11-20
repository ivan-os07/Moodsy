package ru.urfu.service.analytics;

import ru.urfu.model.MoodEntry;
import ru.urfu.model.Sphere;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Сервис расчета статистики записей пользователя
 */
public class MoodAnalyticsService {
    /**
     * Расчитывает среднюю оценку, лучший и худший дни, и наиболее частую сферу
     *
     * @param entries непустой список с отметками пользователя
     * @return рассчитанные показатели статистики
     * @throws NullPointerException     если список или одна из его записей null
     * @throws IllegalArgumentException если список пуст
     */
    public MoodStatistics calculate(List<MoodEntry> entries) {
        Objects.requireNonNull(entries, "entries не может быть null");
        if (entries.isEmpty()) {
            throw new IllegalArgumentException("Невозможно рассчитать статистику по пустому списку");
        }

        long scoreSum = 0;
        MoodEntry bestDay = null;
        MoodEntry worstDay = null;

        EnumMap<Sphere, SphereAccumulator> sphereStats = new EnumMap<>(Sphere.class);

        Sphere mostFrequentSphere = null;
        SphereAccumulator mostFrequentStats = null;
        for (MoodEntry entry : entries) {
            Objects.requireNonNull(entry, "entries не должен содержать null");
            Objects.requireNonNull(entry.sphere(), "sphere не может быть null");
            scoreSum += entry.score();
            if (bestDay == null || isBetter(entry, bestDay)) {
                bestDay = entry;
            }
            if (worstDay == null || isWorse(entry, worstDay)) {
                worstDay = entry;

            }
            Sphere sphere = entry.sphere();
            SphereAccumulator currentStats = sphereStats.computeIfAbsent(
                    sphere, k -> new SphereAccumulator()
            );

            currentStats.add(entry.createdAt());

            if (mostFrequentStats == null
                    || currentStats.count > mostFrequentStats.count
                    || currentStats.count == mostFrequentStats.count
                    && currentStats.latestCreatedAt.isAfter(
                    mostFrequentStats.latestCreatedAt)) {

                mostFrequentSphere = sphere;
                mostFrequentStats = currentStats;

            }

        }

        double averageScore = (double) scoreSum / entries.size();

        assert mostFrequentStats != null;
        return new MoodStatistics(
                averageScore,
                bestDay,
                worstDay,
                mostFrequentSphere,
                mostFrequentStats.count
        );


    }

    /**
     * Проверяет, является ли запись лучшей.
     * При одинаковых оценках выбирается более ранняя запись.
     */
    private boolean isBetter(MoodEntry candidate, MoodEntry current) {
        return candidate.score() > current.score()
                || (candidate.score() == current.score()
                && candidate.createdAt().isBefore(current.createdAt()));
    }

    /**
     * Проверяет, является ли запись худшей.
     * При одинаковых оценках выбирается более ранняя запись.
     */
    private boolean isWorse(MoodEntry candidate, MoodEntry current) {
        return candidate.score() < current.score()
                || (candidate.score() == current.score()
                && candidate.createdAt().isBefore(current.createdAt()));
    }

    /**
     * Накопитель под статистику отдельной сферы
     */
    private class SphereAccumulator {
        /**
         * Количество отметок в сфере
         */
        private int count;
        /**
         * Время последней отметки в сфере
         */
        private LocalDateTime latestCreatedAt;

        /**
         * Учитывает очередную заметку и обновляет время последнего использования сферы
         *
         * @param createdAt дата и время создания отметки
         */
        private void add(LocalDateTime createdAt) {
            count++;

            if (latestCreatedAt == null
                    || createdAt.isAfter(latestCreatedAt)) {
                latestCreatedAt = createdAt;
            }
        }
    }
}
