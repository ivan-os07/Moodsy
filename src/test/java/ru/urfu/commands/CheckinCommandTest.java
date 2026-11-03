package ru.urfu.commands;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.fsm.CheckinFsmService;
import ru.urfu.service.fsm.InMemoryCheckinSessionStore;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Тестирование команды /checkin.
 */
class CheckinCommandTest {

    /**
     * Сообщение о вызове команды с параметрами.
     */
    private static final String ARGS_ERROR =
            "❌ Команда /checkin вызывается без параметров.";

    /**
     * Идентификатор пользователя.
     */
    private final UUID userId = UUID.fromString("00000000-0000-0000-0000-000000006767");

    /**
     * Хранилище сессий чекина.
     */
    private InMemoryCheckinSessionStore sessionStore;

    /**
     * Репозиторий записей настроения.
     */
    private InMemoryMoodRepository moodRepository;

    /**
     * Тестируемая команда.
     */
    private CheckinCommand command;

    /**
     * Создаёт команду с реальным сервисом чекина на in-memory зависимостях.
     */
    @BeforeEach
    void setUp() {
        sessionStore = new InMemoryCheckinSessionStore();
        moodRepository = new InMemoryMoodRepository();
        Clock clock = Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneId.of("Europe/Moscow"));
        CheckinFsmService checkinFsmService = new CheckinFsmService(sessionStore, moodRepository, clock);
        command = new CheckinCommand(checkinFsmService);
    }

    /**
     * Проверяет, что команда без параметров запускает диалог и не сохраняет запись.
     */
    @Test
    void execute_withoutArgs_startsDialog() {
        Assertions.assertEquals("Введите оценку от 1 до 10",
                command.execute(userId, new String[]{}));

        Assertions.assertTrue(sessionStore.hasSession(userId));
        Assertions.assertTrue(moodRepository.findLast(userId, 10).isEmpty());
    }

    /**
     * Проверяет, что вызов с любыми параметрами возвращает ошибку
     * и не создаёт сессию.
     */
    @Test
    void execute_withArgs_returnsArgsError() {
        Assertions.assertEquals(ARGS_ERROR, command.execute(userId, new String[]{"8"}));
        Assertions.assertEquals(ARGS_ERROR, command.execute(userId, new String[]{"abc"}));
        Assertions.assertEquals(ARGS_ERROR, command.execute(userId, new String[]{"6", "7"}));

        Assertions.assertFalse(sessionStore.hasSession(userId));
        Assertions.assertTrue(moodRepository.findLast(userId, 10).isEmpty());
    }

    /**
     * Проверяет ключ и описание команды.
     */
    @Test
    void commandMetadata() {
        Assertions.assertEquals("/checkin", command.getCommandKey());
        Assertions.assertEquals("Пошаговая отметка настроения", command.getDescription());
    }
}
