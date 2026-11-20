package ru.urfu.service.fsm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.model.MoodEntry;
import ru.urfu.repository.InMemoryMoodRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

/**
 * Тестирование конечного автомата checkin
 */
class CheckinFsmServiceTest {

    /**
     * Хранилище сессий чекина в памяти.
     */
    private InMemoryCheckinSessionStore sessionStore;

    /**
     * Репозиторий записей настроения в памяти.
     */
    private InMemoryMoodRepository moodRepository;

    /**
     * Источник фиксированного времени для тестов.
     */
    private Clock clock;

    /**
     * Тестируемый FSM-сервис.
     */
    private CheckinFsmService service;

    /**
     * Идентификатор тестового пользователя.
     */
    private UUID userId;

    /**
     * Конструктор
     */
    @BeforeEach
    void setUp() {
        sessionStore = new InMemoryCheckinSessionStore();
        moodRepository = new InMemoryMoodRepository();
        clock = Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneOffset.UTC);
        service = new CheckinFsmService(sessionStore, moodRepository, clock);
        userId = UUID.randomUUID();
    }

    /**
     * Проверяет, что старт чекина создаёт сессию на шаге SCORE и возвращает приглашение.
     */
    @Test
    void testStart_createsSessionAndAsksForScore() {
        String response = service.start(userId);

        Assertions.assertEquals("Введите оценку от 1 до 10", response);
        Assertions.assertTrue(sessionStore.hasSession(userId));
        Assertions.assertEquals(CheckinStep.SCORE, sessionStore.findById(userId).orElseThrow().step());
    }

    /**
     * Проверяет открыта ли сессия у пользователя.
     */
    @Test
    void testHasActiveSession() {
        Assertions.assertFalse(service.hasActiveSession(userId));

        service.start(userId);

        Assertions.assertTrue(service.hasActiveSession(userId));
    }

    /**
     * Ввод оценки на шаге SCORE (переход к NOTE)
     * Пользователь начал чекин и отправляет число, например "7".
     */
    @Test
    void testHandle_validScore_savesScoreAndAsksForNote() {
        service.start(userId);

        String response = service.handle(userId, "7");

        Assertions.assertEquals("Добавьте заметку или отправьте /skip", response);

        CheckinSession session = sessionStore.findById(userId).orElseThrow();

        Assertions.assertEquals(7, session.score());
        Assertions.assertEquals(CheckinStep.NOTE, session.step());
    }

    /**
     * Ввод невалидного значения(строки) на шаге SCORE → ошибка, остаёмся на SCORE
     * Пользователь начал чекин и отправляет строку, например "abc".
     */
    @Test
    void testHandle_invalidScoreStringAndStayOnSCORE() {
        service.start(userId);

        Assertions.assertEquals("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                service.handle(userId, "abc")
        );
        Assertions.assertEquals("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                service.handle(userId, "")
        );

        // Остаемся на SCORE
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SCORE, session.step());
    }

    /**
     * Ввод невалидного значения(не из диапазона) на шаге SCORE → ошибка, остаёмся на SCORE
     * Пользователь начал чекин и отправляет число не из диапазона, например "11".
     */
    @Test
    void testHandle_invalidScoreIntAndStayOnSCORE() {
        service.start(userId);

        Assertions.assertEquals("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                service.handle(userId, "0")
        );
        Assertions.assertEquals("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                service.handle(userId, "11")
        );
        Assertions.assertEquals("❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                service.handle(userId, "100")
        );

        // Остаемся на SCORE
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SCORE, session.step());
    }

    /**
     * Успешный ввод строки для заметки на шаге NOTE → сохраняет заметку, переход в PREVIEW
     */
    @Test
    void testHandle_validNOTE() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        String response = service.handle(userId, "очень много тикетов");

        String expected = "\uD83D\uDCCB Предпросмотр карточки:\n• Оценка: 7/10\n• Заметка: очень много тикетов\nПодтвердите запись:\n/approve — сохранить\n/reject — отменить";
        Assertions.assertEquals(expected, response);

        // Переходим в PREVIEW
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Успешный ввод пустой строки(=команде skip) на шаге NOTE → пустая заметка, переход в PREVIEW
     */
    @Test
    void testHandle_validEmptyNOTE() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        String response = service.handle(userId, "");

        String expected = "\uD83D\uDCCB Предпросмотр карточки:\n• Оценка: 7/10\n• Заметка:\nПодтвердите запись:\n/approve — сохранить\n/reject — отменить";
        Assertions.assertEquals(expected, response);

        // Переходим в PREVIEW
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Успешный ввод команды skip на шаге NOTE → пустая заметка, переход в PREVIEW
     */
    @Test
    void testHandle_validCmdSkip() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        String response = service.handle(userId, "/skip");

        String expected = "\uD83D\uDCCB Предпросмотр карточки:\n• Оценка: 7/10\n• Заметка:\nПодтвердите запись:\n/approve — сохранить\n/reject — отменить";
        Assertions.assertEquals(expected, response);

        // Переходим в PREVIEW
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Неуспешный ввод команды skip на шаге не NOTE, и не на PREVIEW → сообщение об ошибке
     */
    @Test
    void testHandle_invalidCmdSkipNotPREVIEW() {
        String expected = "❌ Ошибка: команда /skip доступна только при вводе заметки.";

        // Например, на шаге SCORE
        service.start(userId);
        String response = service.handle(userId, "/skip");
        Assertions.assertEquals(expected, response);

        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SCORE, session.step());
    }

    /**
     * Неуспешный ввод команды skip на шаге PREVIEW → сообщение об ошибке и доступные шаги
     */
    @Test
    void testHandle_invalidCmdSkipPREVIEW() {
        String expected = "❌ Ошибка: команда /skip доступна только при вводе заметки.\nПожалуйста, подтвердите запись с помощью /approve или отмените через /reject.";

        // Идем к шагу PREVIEW
        service.start(userId);
        service.handle(userId, "1");
        service.handle(userId, "ZAMETKA");


        String response = service.handle(userId, "/skip");
        Assertions.assertEquals(expected, response);

        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Сохранение заметки при использовании /approve на шаге PREVIEW → сохраняет MoodEntry, удаляет сессию
     */
    @Test
    void testHandle_save() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        // Успешный пройденный шаг NOTE
        service.handle(userId, "заметка");

        String response = service.handle(userId, "/approve");
        Assertions.assertEquals("✅ Запись сохранена.", response);

        // Проверяем, что запись появилась в репозитории
        List<MoodEntry> savedEntries = moodRepository.findLast(userId, 1);
        Assertions.assertEquals(1, savedEntries.size());

        MoodEntry savedEntry = savedEntries.getFirst();

        Assertions.assertEquals(7, savedEntry.score());
        Assertions.assertEquals("заметка", savedEntry.note());
        Assertions.assertEquals(LocalDateTime.now(clock.withZone(ZoneOffset.ofHours(3))), savedEntry.createdAt());

        // Сессия закрылась
        Assertions.assertFalse(service.hasActiveSession(userId));
    }

    /**
     * Выход из диалога по команде /exit: сессия удаляется, запись не сохраняется.
     */
    @Test
    void testHandle_exitCancelsSessionWithoutSaving() {
        service.start(userId);
        service.handle(userId, "7"); // находимся на шаге NOTE

        // до действия
        List<MoodEntry> before = moodRepository.findLast(userId, 100);

        String response = service.handle(userId, "/exit");

        // после действия; проверяем, что не добавились записи
        List<MoodEntry> after = moodRepository.findLast(userId, 100);
        // сравнение корректное тк используем record (сравнивает объекты по значению всех их полей)
        Assertions.assertEquals(before, after);

        Assertions.assertEquals("Сессия завершена без сохранения отметки", response);
        Assertions.assertFalse(sessionStore.hasSession(userId));

    }

    /**
     * Отмена записи по команде /reject на шаге PREVIEW: сессия удаляется, запись не сохраняется.
     */
    @Test
    void testHandle_rejectCancelsSessionWithoutSaving() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "заметка");

        // до действия
        List<MoodEntry> before = moodRepository.findLast(userId, 100);

        String response = service.handle(userId, "/reject");

        // после действия; проверяем, что не добавились записи
        List<MoodEntry> after = moodRepository.findLast(userId, 100);
        // сравнение корректное тк используем record (сравнивает объекты по значению всех их полей)
        Assertions.assertEquals(before, after);

        Assertions.assertEquals("Сессия завершена без сохранения отметки", response);
        Assertions.assertFalse(sessionStore.hasSession(userId));
    }

    /**
     * Возврат назад с шага NOTE на шаг SCORE по команде /back.
     */
    @Test
    void testHandle_backFromNoteToScore() {
        service.start(userId);
        service.handle(userId, "7"); // перешли на NOTE

        String response = service.handle(userId, "/back");

        Assertions.assertEquals("Введите оценку от 1 до 10", response);
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SCORE, session.step());
    }

    /**
     * Возврат назад с шага PREVIEW на шаг NOTE по команде /back.
     */
    @Test
    void testHandle_backFromPreviewToNote() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "заметка"); // перешли на PREVIEW

        String response = service.handle(userId, "/back");

        Assertions.assertEquals("Добавьте заметку или отправьте /skip", response);
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.NOTE, session.step());
    }

    /**
     * Попытка возврата назад с начального шага SCORE: возвращает ошибку и остаётся на SCORE.
     */
    @Test
    void testHandle_backFromScoreReturnsError() {
        service.start(userId); // находимся на SCORE

        String response = service.handle(userId, "/back");

        Assertions.assertEquals("❌ Вы на первом шаге, вернуться назад нельзя.", response);
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SCORE, session.step());
    }

    /**
     * Некорректный ввод на шаге PREVIEW: напоминает о доступных командах.
     */
    @Test
    void testHandle_invalidInputOnPreview() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "заметка");

        String response = service.handle(userId, "что-то другое");

        Assertions.assertEquals("Пожалуйста, подтвердите запись с помощью /approve или отмените через /reject.",
                response
        );
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }
}
