package ru.urfu.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.model.MoodEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;
import ru.urfu.model.Sphere;


/**
 * Тестирование методов работы с хранилищем
 */
class InMemoryMoodRepositoryTest {
    /**
     * Репозиторий для хранения отметок
     */
    private InMemoryMoodRepository repository;
    /**
     * Идентификатор пользователя
     */
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000006767");
    /**
     * Первая заметка для тестирования
     */
    private final MoodEntry firstEntry = new MoodEntry(LocalDateTime.of(2026, 10, 10, 10, 0), 5, Sphere.WORK, "");
    /**
     * Вторая заметка для тестирования
     */
    private final MoodEntry secondEntry = new MoodEntry(LocalDateTime.of(2026, 10, 11, 11, 11), 8, Sphere.WORK, "");
    /**
     * Третья заметка для тестирования
     */
    private final MoodEntry thirdEntry = new MoodEntry(LocalDateTime.of(2026, 10, 12, 12, 12), 1, Sphere.WORK, "");

    @BeforeEach
    void setUp() {
        repository = new InMemoryMoodRepository();
    }

    /**
     * Проверяет, что в репозиторий .save() сохраняет запись (одну или несколько)
     * и после этого, ее можно получить несколько через findLast, в правильном порядке
     * от новых к старым
     */
    @Test
    void testSaveOneOrMoreEntries() {
        repository.save(userId, firstEntry);
        repository.save(userId, secondEntry);
        repository.save(userId, thirdEntry);
        Assertions.assertEquals(List.of(thirdEntry, secondEntry, firstEntry), repository.findLast(userId, 10));
    }


    /**
     * Проверяет, findLast
     * Порядок вывода limit >= кол-во entries
     * Порядок вывода limit < кол-во entries
     * Значения limit отличные от положительных
     * unknown userId
     */
    @Test
    void testFindLastLimitTests() {
        repository.save(userId, firstEntry);
        repository.save(userId, secondEntry);
        repository.save(userId, thirdEntry);
        Assertions.assertEquals(List.of(thirdEntry, secondEntry, firstEntry), repository.findLast(userId, 10));
        Assertions.assertEquals(List.of(thirdEntry, secondEntry), repository.findLast(userId, 2));
        Assertions.assertTrue(repository.findLast(userId, 0).isEmpty());
        Assertions.assertTrue(repository.findLast(userId, -1).isEmpty());
        Assertions.assertTrue(repository.findLast(userId, -66).isEmpty());
        Assertions.assertTrue(repository.findLast(UUID.fromString("00000000-0000-0000-0000-000000000000"),
                10
        ).isEmpty());
    }


    /**
     * Проверяет, что записи одного пользователя не попадают в хранилище заметок другого пользователя.
     */
    @Test
    void testSecondUser() {
        UUID secondUserId = UUID.fromString("00000000-0000-0000-0000-000000000911");

        repository.save(userId, firstEntry);
        repository.save(secondUserId, secondEntry);

        Assertions.assertEquals(List.of(firstEntry), repository.findLast(userId, 10));
        Assertions.assertEquals(List.of(secondEntry), repository.findLast(secondUserId, 10));
    }

    /**
     * Проверяет, что невозможно изменить отметки пользователя из вне.
     */
    @Test
    void testUnmodifiableList() {
        repository.save(userId, firstEntry);

        List<MoodEntry> history = repository.findLast(userId, 10);

        UnsupportedOperationException exception = Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> history.add(secondEntry)
        );
        // Стандартное поведение неизменяемой коллекции
        Assertions.assertNull(exception.getMessage());
        Assertions.assertIterableEquals(List.of(firstEntry),
                repository.findLast(userId, 10)
        );
    }

    /**
     * Тестируем, что начало периода включительно - конец исключительно
     * содержатся в return метода
     */
    @Test
    void testFindByPeriodInclusiveStartAndExclusiveEnd() {
        MoodEntry before = new MoodEntry(
                LocalDateTime.of(2026, 11, 1, 23, 59),
                5, Sphere.WORK, "");
        MoodEntry atStart = new MoodEntry(
                LocalDateTime.of(2026, 11, 2, 00, 00),
                5, Sphere.REST, "");

        MoodEntry beforeEnd = new MoodEntry(
                LocalDateTime.of(2026, 11, 8, 23, 59),
                5, Sphere.HEALTH, "");
        MoodEntry atEnd = new MoodEntry(
                LocalDateTime.of(2026, 11, 9, 00, 00),
                5, Sphere.SOCIAL, "");
        repository.save(userId, before);
        repository.save(userId, atStart);
        repository.save(userId, atEnd);
        repository.save(userId, beforeEnd);
        List<MoodEntry> result = repository.findByPeriod(userId,
                LocalDateTime.of(2026, 11, 2, 0, 0),
                LocalDateTime.of(2026, 11, 9, 0, 0));
        Assertions.assertIterableEquals(List.of(atStart, beforeEnd), result);
    }

    /**
     * Тестируем, что при отсутствии отметок за период возвращается пустой список
     */
    @Test
    void testFindByPeriodReturnsEmptyList() {
        List<MoodEntry> result = repository.findByPeriod(userId,
                LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0));
        Assertions.assertTrue(result.isEmpty());
    }

    /**
     * Тестируем, что исключение при нарушении
     * условия: начало периода должно быть раньше конца
     */
    @Test
    void testFindByPeriodInvalidRange() {
        LocalDateTime date = LocalDateTime.of(2026, 11, 1, 0, 0);
        IllegalArgumentException exception = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.findByPeriod(userId, date, date));
        Assertions.assertEquals("Начало периода должно быть раньше конца",
                exception.getMessage());
        IllegalArgumentException exceptionEndExclusive = Assertions.assertThrows(
                IllegalArgumentException.class,
                () -> repository.findByPeriod(userId, date.plusDays(1), date));
        Assertions.assertEquals("Начало периода должно быть раньше конца",
                exceptionEndExclusive.getMessage());
    }
    @Test
    void testFindByPeriodRejectsNullArguments(){
        LocalDateTime from = LocalDateTime.of(2026, 11, 1, 0, 0);
        LocalDateTime to = LocalDateTime.of(2026, 12, 1, 0, 0);
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> repository.findByPeriod(null, from, to));
        Assertions.assertEquals("userId не может быть null",
                exception.getMessage());

        NullPointerException exceptionFrom = Assertions.assertThrows(
                NullPointerException.class,
                () -> repository.findByPeriod(userId, null, to));
        Assertions.assertEquals("from не может быть null",
                exceptionFrom.getMessage());

        NullPointerException exceptionTo = Assertions.assertThrows(
                NullPointerException.class,
                () -> repository.findByPeriod(userId, from, null));
        Assertions.assertEquals("to не может быть null",
                exceptionTo.getMessage());
    }
}
