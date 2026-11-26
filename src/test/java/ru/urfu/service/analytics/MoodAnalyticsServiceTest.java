package ru.urfu.service.analytics;


import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ru.urfu.model.MoodEntry;
import ru.urfu.model.Sphere;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Тестируем расчет статистики настроения
 */
class MoodAnalyticsServiceTest {
    /**
     * Сервис расчета статистики
     */
    private MoodAnalyticsService service;

    @BeforeEach
    void setUp() {
        service = new MoodAnalyticsService();
    }

    /**
     * Тестируем правильность расчета средней оценки
     */
    @Test
    void testCalculateAverageScore() {
        List<MoodEntry> entries = List.of(entry(5, 1, Sphere.WORK),
                entry(7, 2, Sphere.HEALTH),
                entry(9, 3, Sphere.REST)
        );

        MoodStatistics result = service.calculate(entries);
        Assertions.assertEquals(7.0, result.averageScore());
    }

    /**
     * Тестируем выбор записей с максимальной и минимальной оценками
     */
    @Test
    void testCalculateBestAndWorstDay() {
        MoodEntry best = entry(10, 2, Sphere.WORK);
        MoodEntry worst = entry(1, 1, Sphere.HEALTH);

        List<MoodEntry> entries = List.of(entry(5, 3, Sphere.REST),
                best,
                worst);

        MoodStatistics result = service.calculate(entries);
        Assertions.assertEquals(best, result.bestDay());
        Assertions.assertEquals(worst, result.worstDay());
    }

    /**
     * Тестируем, что при одинаковой максимальной оценке выбирается более ранняя запись
     */
    @Test
    void testBestDayTieUsesEarlierEntry() {
        MoodEntry earlier = entry(10, 1, Sphere.WORK);
        MoodEntry later = entry(10, 2, Sphere.HEALTH);

        MoodStatistics result = service.calculate(List.of(later, earlier));
        Assertions.assertEquals(earlier, result.bestDay());
    }

    /**
     * Тестируем, что при одинаковой минимальной оценке выбирается более ранняя запись.
     */
    @Test
    void testWorstDayTieUsesEarlierEntry() {
        MoodEntry earlier = entry(1, 1, Sphere.WORK);
        MoodEntry later = entry(1, 2, Sphere.HEALTH);

        MoodStatistics result = service.calculate(List.of(later, earlier));
        Assertions.assertEquals(earlier, result.worstDay());
    }

    /**
     * Тестируем выбор наиболее часто встречающейся сферы.
     */
    @Test
    void testCalculateMostFrequentSphere() {
        List<MoodEntry> entries = List.of(
                entry(5, 1, Sphere.WORK),
                entry(7, 2, Sphere.HEALTH),
                entry(8, 3, Sphere.WORK),
                entry(6, 4, Sphere.REST)
        );

        MoodStatistics result = service.calculate(entries);
        Assertions.assertEquals(Sphere.WORK, result.mostFrequentSphere());
        Assertions.assertEquals(2, result.sphereCount());
    }

    /**
     * Тестируем разрешение равенства по наиболее поздней записи в сфере
     */
    @Test
    void testSphereTieUsesMostRecentEntry() {
        List<MoodEntry> entries = List.of(
                entry(5, 1, Sphere.WORK),
                entry(7, 2, Sphere.HEALTH),
                entry(8, 3, Sphere.HEALTH),
                entry(6, 4, Sphere.WORK));

        MoodStatistics result = service.calculate(entries);
        Assertions.assertEquals(Sphere.WORK, result.mostFrequentSphere());
        Assertions.assertEquals(2, result.sphereCount());
    }

    /**
     * Тестируем исключение при передаче null вместо списка записей
     */
    @Test
    void testCalculateRejectsNullEntries() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> service.calculate(null)
        );

        Assertions.assertEquals(
                "entries не может быть null",
                exception.getMessage());
    }

    /**
     * Тестируем исключение при пустом списке записей
     */
    @Test
    void testCalculateRejectsEmptyEntries() {
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> service.calculate(List.of())
        );

        Assertions.assertEquals("Невозможно рассчитать статистику по пустому списку",
                exception.getMessage());
    }

    /**
     * Тестируем на исключение если есть null среди записей
     */
    @Test
    void testCalculateRejectsNullEntry() {
        List<MoodEntry> entries = Arrays.asList(entry(5, 1, Sphere.WORK), null);

        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> service.calculate(entries)
        );

        Assertions.assertEquals(
                "entries не должен содержать null",
                exception.getMessage()
        );
    }

    /**
     * Тестируем ситуацию, когда записи есть только с одной сферой
     */
    @Test
    void testCalculateWithSingleSphere() {
        List<MoodEntry> entries = List.of(entry(5, 1, Sphere.WORK),
                entry(7, 2, Sphere.WORK),
                entry(9, 3, Sphere.WORK),
                entry(5, 4, Sphere.WORK));
        MoodStatistics result = service.calculate(entries);
        Assertions.assertEquals(Sphere.WORK, result.mostFrequentSphere());
        Assertions.assertEquals(4, result.sphereCount());
    }

    /**
     * Создает запись настроения для тестирования
     *
     * @param score  оценка настроения
     * @param day    день создания записи в ноябре 2026 года
     * @param sphere сфера жизни
     * @return запись настроения
     */
    private MoodEntry entry(int score, int day, Sphere sphere) {
        /* Если везде вставлять LocalDateTime.of получается слишком громоздко;
        поэтому вынесли в отдельный метод*/
        return new MoodEntry(LocalDateTime.of(2026, 11, day, 12, 0), score, sphere, ""
        );
    }
}

