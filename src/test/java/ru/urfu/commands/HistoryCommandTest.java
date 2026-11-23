package ru.urfu.commands;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.model.MoodEntry;
import ru.urfu.model.Sphere;
import ru.urfu.repository.InMemoryMoodRepository;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Assertions;

/**
 * Тестирование команды /history
 */
class HistoryCommandTest {
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
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000006767");

    @BeforeEach
    void setUp() {
        repository = new InMemoryMoodRepository();
        command = new HistoryCommand(repository);

    }

    /**
     * Тестируем:
     * попытки обращения к пустой истории (с аргументом и без)
     * корректности формирования строки для ответа пользователю (с лимитом и без)
     * проверка корректного отображения в истории (от старых к новым)
     */
    @Test
    void testExecute() {
        Assertions.assertEquals("История пуста. Попробуйте использовать команду /checkin",
                command.execute(userId, null)
        );
        Assertions.assertEquals("История пуста. Попробуйте использовать команду /checkin",
                command.execute(userId, new String[]{"6"})
        );

        // пустые заметки
        repository.save(userId, new MoodEntry(LocalDateTime.of(2026, 10, 10, 10, 10), 10, Sphere.WORK, ""));
        repository.save(userId, new MoodEntry(LocalDateTime.of(2026, 10, 11, 13, 12), 1, Sphere.WORK, ""));
        repository.save(userId, new MoodEntry(LocalDateTime.of(2026, 11, 12, 23, 56), 6, Sphere.WORK, ""));

        String response = command.execute(userId, null);
        String expected = """
                 \uD83D\uDCC5 Ваша история записей:
                12.11.2026
                23:56 - 6/10 - Дела
                11.10.2026
                13:12 - 1/10 - Дела
                10.10.2026
                10:10 - 10/10 - Дела""";

        Assertions.assertEquals(expected, response);
        String response2 = command.execute(userId, new String[]{"2"});
        String expected2 = """
                 \uD83D\uDCC5 Ваша история записей:
                12.11.2026
                23:56 - 6/10 - Дела
                11.10.2026
                13:12 - 1/10 - Дела""";
        Assertions.assertEquals(expected2, response2);
        Assertions.assertFalse(response2.contains("10.10.2026 10:10 - 10/10"));
    }

    /**
     * Тестируем: что при использовании команды без аргумента
     * и наличия кол-ва заметок в истории большем чем 10
     * выводится именно 10 заметок.
     */
    @Test
    void testExecuteTestLimit() {
        for (int i = 1; i <= 23; i++) {
            repository.save(userId, new MoodEntry(LocalDateTime.of(2026, 10, i, 10, 0), i % 10 + 1, Sphere.WORK, ""));
        }
        String response = command.execute(userId, null);
        long entriesCount = response.lines().filter(line -> line.matches("\\d{2}\\.\\d{2}\\.\\d{4}.*")).count();
        Assertions.assertEquals(10, entriesCount);
    }

    /**
     * Тестируем, сообщения об ошибках в сценариях:
     * буквенная последовательность, отрицательные числа, нуль, несколько аргументов
     */
    @Test
    void testExecuteError() {
        String exp = "❌ Ошибка: переданный формат числа не соответствует ожидаемому";
        Assertions.assertEquals(exp, command.execute(userId, new String[]{"abc"}));
        Assertions.assertEquals(exp, command.execute(userId, new String[]{"-1"}));
        Assertions.assertEquals(exp, command.execute(userId, new String[]{"0"}));
        Assertions.assertEquals(exp, command.execute(userId, new String[]{"-67"}));
        Assertions.assertEquals("""
                Использование команды /history:
                /history <n> - показать последние n записей
                /history - последние 10 записей""", command.execute(userId, new String[]{"6", "7"})
        );
    }


    /**
     * Тестируем, что заметка выводится в верном формате.
     */
    @Test
    void testExecuteShowsNoteAndKeepsTimeAsStored() {
        repository.save(userId, new MoodEntry(LocalDateTime.of(2026, 12, 12, 23, 56), 6, Sphere.WORK, "много тикетов"));

        String response = command.execute(userId, null);

        // проверка с заметкой
        Assertions.assertEquals("""
                 📅 Ваша история записей:
                12.12.2026
                23:56 - 6/10 - Дела
                Заметка: много тикетов""", response);

        long noteLines = response.lines().filter(line -> line.startsWith("Заметка:")).count();
        Assertions.assertEquals(1, noteLines);
    }
}
