package ru.urfu.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.model.MoodEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


import org.junit.jupiter.api.Assertions;


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
    private final MoodEntry firstEntry = new MoodEntry(LocalDateTime.of(2026, 10, 10, 10, 0), 5, "");
    /**
     * Вторая заметка для тестирования
     */
    private final MoodEntry secondEntry = new MoodEntry(LocalDateTime.of(2026, 10, 11, 11, 11), 8, "");
    /**
     * Третья заметка для тестирования
     */
    private final MoodEntry thirdEntry = new MoodEntry(LocalDateTime.of(2026, 10, 12, 12, 12), 1, "");

    @BeforeEach
    void setUp() {
        repository = new InMemoryMoodRepository();
    }

    /**
     * Проверяет, что в репозиторий .save() сохраняет запись (одну или несколько)
     * и после этого, ее можно получить через findLatest,
     * или получить несколько через findLast, в правильном порядке
     * от новых к старым
     */
    @Test
    void saveOneOrMoreEntries() {
        repository.save(userId, firstEntry);
        Assertions.assertEquals(
                Optional.of(firstEntry),
                repository.findLatest(userId)
        );
        repository.save(userId, secondEntry);
        repository.save(userId, thirdEntry);
        Assertions.assertEquals(
                List.of(thirdEntry, secondEntry, firstEntry),
                repository.findLast(userId, 10)
        );
    }


    /**
     * Проверяет, findLast
     * Порядок вывода limit >= кол-во entries
     * Порядок вывода limit < кол-во entries
     * Значения limit отличные от положительных
     * unknown userId
     */
    @Test
    void findLastLimitTests() {
        repository.save(userId, firstEntry);
        repository.save(userId, secondEntry);
        repository.save(userId, thirdEntry);
        Assertions.assertEquals(
                List.of(thirdEntry, secondEntry, firstEntry),
                repository.findLast(userId, 10)
        );
        Assertions.assertEquals(
                List.of(thirdEntry, secondEntry),
                repository.findLast(userId, 2)
        );
        Assertions.assertTrue(repository.findLast(userId, 0).isEmpty());
        Assertions.assertTrue(repository.findLast(userId, -1).isEmpty());
        Assertions.assertTrue(repository.findLast(userId, -66).isEmpty());
        Assertions.assertTrue(repository.findLast(UUID.fromString("00000000-0000-0000-0000-000000000000"), 10).isEmpty());
    }

    /**
     * Проверяет, findLatest() корректно реагирует на пустую историю (отметок нет),
     * несуществующий userId,
     * и возвращает правильную заметку при стандартном случае сохранения
     *
     */
    @Test
    void findLatestTest() {
        Assertions.assertEquals(Optional.empty(), repository.findLatest(userId));
        repository.save(userId, firstEntry);
        repository.save(userId, secondEntry);
        Assertions.assertEquals(
                Optional.of(secondEntry),
                repository.findLatest(userId)
        );
        Assertions.assertEquals(Optional.empty(), repository.findLatest(UUID.fromString("00000000-0000-0000-0000-000000000000")));
    }


    /**
     * Проверяет, что записи одного пользователя не попадают в хранилище заметок другого пользователя.
     */
    @Test
    void secondUser() {
        UUID secondUserId = UUID.fromString("00000000-0000-0000-0000-000000000911");

        repository.save(userId, firstEntry);
        repository.save(secondUserId, secondEntry);

        Assertions.assertEquals(
                List.of(firstEntry),
                repository.findLast(userId, 10)
        );
        Assertions.assertEquals(
                List.of(secondEntry),
                repository.findLast(secondUserId, 10)
        );
    }

    /**
     * Проверяет, что невозможно изменить отметки пользователя из вне.
     */
    @Test
    void unmodifiableList() {
        repository.save(userId, firstEntry);

        List<MoodEntry> history = repository.findLast(userId, 10);
        Assertions.assertThrows(UnsupportedOperationException.class, () -> history.add(secondEntry));
    }
}
