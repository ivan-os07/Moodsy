package ru.urfu.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.service.MoodEntry;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;


class InMemoryMoodRepositoryTest {
    private InMemoryMoodRepository repository;
    private final String userId = "tg:6767";

    private final MoodEntry firstEntry = new MoodEntry(LocalDateTime.of(2026, 10, 10, 10, 0), 5);
    private final MoodEntry secondEntry = new MoodEntry(LocalDateTime.of(2026, 10, 11, 11, 11), 8);
    private final MoodEntry thirdEntry = new MoodEntry(LocalDateTime.of(2026, 10, 12, 12, 12), 1);

    /**
     * Создаем новый репозиторий перед каждым тестом,
     * чтобы не копить результаты в одном
     */
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
        assertEquals(
                Optional.of(firstEntry),
                repository.findLatest(userId)
        );
        repository.save(userId, secondEntry);
        repository.save(userId, thirdEntry);
        assertEquals(
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
        assertEquals(
                List.of(thirdEntry, secondEntry, firstEntry),
                repository.findLast(userId, 10)
        );
        assertEquals(
                List.of(thirdEntry, secondEntry),
                repository.findLast(userId, 2)
        );
        assertTrue(repository.findLast(userId, 0).isEmpty());
        assertTrue(repository.findLast(userId, -1).isEmpty());
        assertTrue(repository.findLast(userId, -66).isEmpty());
        assertTrue(repository.findLast("unknown", 10).isEmpty());
    }


    @Test
    void findLatestTest() {
        assertEquals(Optional.empty(), repository.findLatest(userId));
        repository.save(userId, firstEntry);
        repository.save(userId, secondEntry);
        assertEquals(
                Optional.of(secondEntry),
                repository.findLatest(userId)
        );
        assertEquals(Optional.empty(), repository.findLatest("unknown"));
    }

    @Test
    void updateListTest() {
        assertFalse(repository.updateLast(userId, firstEntry));
        repository.save(userId, firstEntry);
        repository.save(userId, secondEntry);
        MoodEntry updatedEntry = new MoodEntry(LocalDateTime.of(2026, 10, 10, 21, 59), 10);

        assertTrue(repository.updateLast(userId, updatedEntry));
        assertEquals(Optional.of(updatedEntry),
                repository.findLatest(userId));
        assertEquals(List.of(updatedEntry, firstEntry), repository.findLast(userId, 2));
        assertFalse(repository.updateLast("unknown", updatedEntry));
    }

    @Test
    void deleteTest() {
        assertFalse(repository.deleteLast(userId));
        repository.save(userId, firstEntry);
        repository.save(userId, secondEntry);
        assertTrue(repository.deleteLast(userId));
        assertEquals(Optional.of(firstEntry), repository.findLatest(userId));
        assertTrue(repository.deleteLast(userId));
        assertTrue(repository.findLatest(userId).isEmpty());
        assertTrue(repository.findLast(userId, 10).isEmpty());
        assertFalse(repository.deleteLast(userId));
    }

    @Test
    void secondUser() {
        String secondUserId = "tg:911";

        repository.save(userId, firstEntry);
        repository.save(secondUserId, secondEntry);

        assertEquals(
                List.of(firstEntry),
                repository.findLast(userId, 10)
        );
        assertEquals(
                List.of(secondEntry),
                repository.findLast(secondUserId, 10)
        );
    }

    @Test
    void UnmodifiableList() {
        repository.save(userId, firstEntry);

        List<MoodEntry> history = repository.findLast(userId, 10);
        assertThrows(UnsupportedOperationException.class, () -> history.add(secondEntry));
    }
}
