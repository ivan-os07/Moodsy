package ru.urfu.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.repository.InMemoryMoodRepository;

import static org.junit.jupiter.api.Assertions.*;

class CheckinCommandTest {
    /**
     * Репозиторий для хранения отметок
     */
    private InMemoryMoodRepository repository;
    /**
     * Сама команда /checkin
     */
    private CheckinCommand command;
    /**
     * Идентификатор пользователя
     */
    private final String userId = "tg:6767";
    /**
     * Фиксированные часы, чтобы тесты не падали
     */
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneId.of("UTC"));

    @BeforeEach
    void setUp() {
        repository = new InMemoryMoodRepository();
        command = new CheckinCommand(repository, clock);

    }

    /**
     * Проверяет правильность формирования сообщений о сохранении отметки
     * дополнительно проверяем правильный порядок в findLast()
     */
    @Test
    void executeTest() {

        assertEquals("✅ Настроение 1 успешно сохранено!", command.execute(userId, new String[]{"1"}));
        MoodEntry expectedEntry = new MoodEntry(LocalDateTime.of(2026, 10, 14, 12, 00), 1);
        assertEquals(Optional.of(expectedEntry), repository.findLatest(userId));
        assertEquals("✅ Настроение 10 успешно сохранено!", command.execute(userId, new String[]{"10"}));
        MoodEntry expectedEntry2 = new MoodEntry(LocalDateTime.of(2026, 10, 14, 12, 00), 10);
        assertEquals(List.of(expectedEntry2, expectedEntry), repository.findLast(userId, 10));
    }

    /**
     * Проверяет, сообщения об ошибке,
     * сценарий null, пустой строки, нескольких аргументов
     * и проверка, что ничего из этого не сохранилось
     */
    @Test
    void executeError1Test(){
        assertEquals("Использование команды /checkin: \n" +
                "/checkin <n> - n число от 1 до 10",
                command.execute(userId, null));
        assertEquals("Использование команды /checkin: \n" +
                        "/checkin <n> - n число от 1 до 10",
                command.execute(userId, new String[]{}));
        assertEquals("Использование команды /checkin: \n" +
                        "/checkin <n> - n число от 1 до 10",
                command.execute(userId, new String[]{"6", "7", "3"}));
        assertTrue(repository.findLast(userId, 10).isEmpty());


    }

    /**
     * Проверяет сообщения об ошибках сценарий
     * слова, буквы
     * и проверка, что ничего из этого не сохранилось
     */
    @Test
    void executeError2Test(){
        assertEquals("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                command.execute(userId, new String[]{"abcdefg"})
                );
        assertEquals("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                command.execute(userId, new String[]{"a"})
        );
        assertTrue(repository.findLast(userId, 10).isEmpty());
    }
    /**
     * Проверка на ошибки,
     * сценарий неправильных чисел
     * и проверка, что ничего из этого не сохранилось
     */
    @Test
    void executeError3Test(){
        assertEquals("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                command.execute(userId, new String[]{"0"})
        );
        assertEquals("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                command.execute(userId, new String[]{"33"})
        );
        assertTrue(repository.findLast(userId, 10).isEmpty());
    }
}
