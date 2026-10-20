package ru.urfu.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.repository.InMemoryMoodRepository;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;

/**
 * Тестирование команды /history
 */
public class HistoryCommandTest {
    /**
     * Репозиторий для хранения отметок
     */
    private InMemoryMoodRepository repository;
    /**
     * Сама команда /history
     */
    private HistoryCommand command;
    /**
     * Идентификатор пользователя
     */
    private final String userId = "tg:6767";

    @BeforeEach
    void setUp() {
        repository = new InMemoryMoodRepository();
        command = new HistoryCommand(repository);

    }

    /**
     * Проверка:
     * попытки обращения к пустой истории (с аргументом и без)
     * корректности формирования строки для ответа пользователю (с лимитом и без)
     * проверка корректного отображения в истории (от старых к новым)
     */
    @Test
    void executeTest() {
        Assertions.assertEquals("История пуста. Попробуйте использовать команду /checkin", command.execute(userId, null));
        Assertions.assertEquals("История пуста. Попробуйте использовать команду /checkin", command.execute(userId, new String[]{"6"}));

        repository.save(userId, new MoodEntry(LocalDateTime.of(2026, 10, 10, 10, 10), 10));
        repository.save(userId, new MoodEntry(LocalDateTime.of(2026, 10, 11, 13, 12), 1));
        repository.save(userId, new MoodEntry(LocalDateTime.of(2026, 11, 12, 23, 56), 6));

        String response = command.execute(userId, null);
        String expected = " \uD83D\uDCC5 Ваша история записей:\n" +
                "12.11.2026 23:56 - 6/10\n" +
                "11.10.2026 13:12 - 1/10\n" +
                "10.10.2026 10:10 - 10/10";
        Assertions.assertEquals(expected, response);
        String response2 = command.execute(userId, new String[]{"2"});
        String expected2 = " \uD83D\uDCC5 Ваша история записей:\n" +
                "12.11.2026 23:56 - 6/10\n" +
                "11.10.2026 13:12 - 1/10";
        Assertions.assertEquals(expected2, response2);
        Assertions.assertFalse(response2.contains("10.10.2026 10:10 - 10/10"));
    }

    /**
     * Проверка, что при использовании команды без аргумента
     * и наличия кол-ва заметок в истории большем чем 10
     * выводится именно 10 заметок.
     */
    @Test
    void executeTestLimit() {
        for (int i = 1; i <= 23; i++) {
            repository.save(userId, new MoodEntry(LocalDateTime.of(2026, 10, i, 10, 0), i % 10 + 1));
        }
        String response = command.execute(userId, null);
        long entriesCount = response.lines().filter(line -> line.matches("\\d{2}\\.\\d{2}\\.\\d{4}.*")).count();
        Assertions.assertEquals(10, entriesCount);
    }

    /**
     * Проверяем, сообщения об ошибках в сценариях:
     * буквенная последовательность, отрицательные числа, нуль, несколько аргументов
     */
    @Test
    void executeErrorTest() {
        String exp = "❌ Ошибка: переданный формат числа не соответствует ожидаемому";
        Assertions.assertEquals(exp, command.execute(userId, new String[]{"abc"}));
        Assertions.assertEquals(exp, command.execute(userId, new String[]{"-1"}));
        Assertions.assertEquals(exp, command.execute(userId, new String[]{"0"}));
        Assertions.assertEquals(exp, command.execute(userId, new String[]{"-67"}));
        Assertions.assertEquals("Использование команды /history: \n " +
                "/history <n> - показать последние n записей\n" +
                "/history - последние 10 записей", command.execute(userId, new String[]{"6", "7"}));
    }
}
