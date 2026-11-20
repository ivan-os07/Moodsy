package ru.urfu.service.fsm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import ru.urfu.model.MoodEntry;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.repository.MoodRepository;

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
    private CheckinSessionStore sessionStore;

    /**
     * Репозиторий записей настроения в памяти.
     */
    private MoodRepository moodRepository;

    /**
     * Тестируемый FSM-сервис.
     */
    private CheckinFsmService service;

    /**
     * Идентификатор тестового пользователя.
     */
    private UUID userId;

    /**
     * Инициализирует зависимости и тестового пользователя перед каждым тестом.
     */
    @BeforeEach
    void setUp() {
        sessionStore = new InMemoryCheckinSessionStore();
        moodRepository = new InMemoryMoodRepository();

        Clock clock = Clock.fixed(Instant.parse("2026-11-02T12:00:00Z"), ZoneOffset.UTC);

        service = new CheckinFsmService(sessionStore, moodRepository, clock);
        userId = UUID.randomUUID();
    }

    /**
     * Проверяет, что старт чекина создаёт сессию на шаге SCORE и возвращает приглашение.
     */
    @Test
    void testStartCreatesSessionAndAsksForScore() {
        String response = service.start(userId);

        Assertions.assertEquals("Введите оценку от 1 до 10", response);
        Assertions.assertTrue(sessionStore.hasSession(userId));
        Assertions.assertEquals(
                CheckinStep.SCORE,
                sessionStore.findById(userId).orElseThrow().step()
        );
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
    void testHandleValidScoreSavesScoreAndAsksForNote() {
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
    @ParameterizedTest
    @ValueSource(strings = {"abc", "", " ", "0", "-1", "11", "100"})
    void testHandleInvalidScoreStaysOnScore(String invalidInput) {
        service.start(userId);

        Assertions.assertEquals(
                "❌ Ошибка: оценка настроения должна быть числом от 1 до 10.",
                service.handle(userId, invalidInput)
        );

        // Остаемся на SCORE
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SCORE, session.step());
    }

    /**
     * Успешный ввод строки для заметки на шаге NOTE → сохраняет заметку, переход в PREVIEW
     */
    @Test
    void testHandleValidNOTE() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        String response = service.handle(userId, "очень много тикетов");

        String expected = """
                📋 Предпросмотр карточки:
                • Оценка: 7/10
                • Заметка: очень много тикетов
                Подтвердите запись:
                /approve — сохранить
                /reject — отменить
                """;

        Assertions.assertEquals(expected, response);

        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        // Проверка, что заметка сохранилась
        Assertions.assertEquals(
                "очень много тикетов",
                session.note()
        );

        // Переходим в PREVIEW
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Успешный ввод пустой строки(=команде skip) на шаге NOTE → пустая заметка, переход в PREVIEW
     */
    @Test
    void testHandleValidEmptyNOTE() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        String response = service.handle(userId, "");

        String expected = """
                📋 Предпросмотр карточки:
                • Оценка: 7/10
                • Заметка:
                Подтвердите запись:
                /approve — сохранить
                /reject — отменить
                """;
        Assertions.assertEquals(expected, response);


        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        // Проверка, что заметка сохранилась
        Assertions.assertEquals(
                "",
                session.note()
        );
        // Переходим в PREVIEW
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Успешный ввод команды skip на шаге NOTE → пустая заметка, переход в PREVIEW
     */
    @Test
    void testHandleValidCmdSkip() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        String response = service.handle(userId, "/skip");

        String expected = """
                📋 Предпросмотр карточки:
                • Оценка: 7/10
                • Заметка:
                Подтвердите запись:
                /approve — сохранить
                /reject — отменить
                """;
        Assertions.assertEquals(expected, response);

        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        // Проверка, что заметка сохранилась
        Assertions.assertEquals(
                "",
                session.note()
        );

        // Переходим в PREVIEW
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Неуспешный ввод команды skip на шаге не NOTE, и не на PREVIEW → сообщение об ошибке
     */
    @Test
    void testHandleInvalidCmdSkipNotPREVIEW() {
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
    void testHandleInvalidCmdSkipPREVIEW() {
        String expected = """
                ❌ Ошибка: команда /skip доступна только при вводе заметки.
                Пожалуйста, подтвердите запись с помощью /approve или отмените через /reject.""";

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
    void testHandleSave() {
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
        Assertions.assertEquals(
                LocalDateTime.of(2026, 11, 2, 15, 0),
                savedEntry.createdAt()
        );

        // Сессия закрылась
        Assertions.assertFalse(service.hasActiveSession(userId));
    }

    /**
     * Выход из диалога по команде /exit: сессия удаляется, запись не сохраняется.
     */
    @Test
    void testHandleExitCancelsSessionWithoutSaving() {
        service.start(userId);
        service.handle(userId, "7"); // находимся на шаге NOTE

        String response = service.handle(userId, "/exit");

        Assertions.assertTrue(moodRepository.findLast(userId, 10).isEmpty());

        Assertions.assertEquals(
                "Сессия завершена без сохранения отметки",
                response
        );
        Assertions.assertFalse(sessionStore.hasSession(userId));
    }

    /**
     * Отмена записи по команде /reject на шаге PREVIEW: сессия удаляется, запись не сохраняется.
     */
    @Test
    void testHandleRejectCancelsSessionWithoutSaving() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "заметка");

        String response = service.handle(userId, "/reject");

        Assertions.assertTrue(moodRepository.findLast(userId, 10).isEmpty());

        Assertions.assertEquals(
                "Сессия завершена без сохранения отметки",
                response
        );
        Assertions.assertFalse(sessionStore.hasSession(userId));
    }

    /**
     * Возврат назад с шага NOTE на шаг SCORE по команде /back.
     */
    @Test
    void testHandleBackFromNoteToScore() {
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
    void testHandleBackFromPreviewToNote() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "заметка"); // перешли на PREVIEW

        String response = service.handle(userId, "/back");

        Assertions.assertEquals(
                "Добавьте заметку или отправьте /skip",
                response
        );
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.NOTE, session.step());
    }

    /**
     * Попытка возврата назад с начального шага SCORE: возвращает ошибку и остаётся на SCORE.
     */
    @Test
    void testHandleBackFromScoreReturnsError() {
        service.start(userId); // находимся на SCORE

        String response = service.handle(userId, "/back");

        Assertions.assertEquals(
                "❌ Вы на первом шаге, вернуться назад нельзя.",
                response
        );
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SCORE, session.step());
    }

    /**
     * Некорректный ввод на шаге PREVIEW: напоминает о доступных командах.
     */
    @Test
    void testHandleInvalidInputOnPreview() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "заметка");

        String response = service.handle(userId, "что-то другое");

        Assertions.assertEquals(
                "Пожалуйста, подтвердите запись с помощью /approve или отмените через /reject.",
                response
        );
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Проверяет, что обработка сообщения без открытой сессии возвращает подсказку.
     */
    @Test
    void testHandleWithoutActiveSessionReturnsNoSession() {
        String response = service.handle(userId, "7");

        Assertions.assertEquals(
                "Диалог не запущен. Отправьте /checkin",
                response
        );
        Assertions.assertFalse(sessionStore.hasSession(userId));
    }

    /**
     * Проверяет, что повторный старт перезаписывает незавершённую сессию.
     */
    @Test
    void testStartOverwritesExistingSession() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "старая заметка");

        String response = service.start(userId);

        Assertions.assertEquals(
                "Введите оценку от 1 до 10",
                response
        );
        CheckinSession session = sessionStore.findById(userId).orElseThrow();

        Assertions.assertEquals(CheckinStep.SCORE, session.step());
        Assertions.assertEquals(0, session.score());
        Assertions.assertEquals("", session.note());
        Assertions.assertTrue(
                moodRepository.findLast(userId, 10).isEmpty()
        );
    }

    /**
     * Проверяет, что неизвестная команда на шаге NOTE возвращает ошибку и не меняет шаг.
     */
    @Test
    void testHandleUnknownCommandOnNoteStaysOnNote() {
        service.start(userId);
        service.handle(userId, "7");

        String response = service.handle(userId, "/unknown");

        Assertions.assertEquals(
                "❌ Неизвестная команда. Доступны: /skip, /back, /exit",
                response
        );
        CheckinSession session = sessionStore.findById(userId).orElseThrow();

        Assertions.assertEquals(CheckinStep.NOTE, session.step());
    }


    /**
     * Проверяет, что граничные оценки и оценка с пробелами по краям принимаются и переводят на шаг NOTE.
     */
    @ParameterizedTest
    @CsvSource({
            "1, 1",
            "10, 10",
            "'  7  ', 7"
    })
    void testHandleValidBoundaryAndTrimmedScore(String input, int expectedScore) {
        service.start(userId);

        String response = service.handle(userId, input);

        Assertions.assertEquals(
                "Добавьте заметку или отправьте /skip",
                response
        );
        CheckinSession session = sessionStore.findById(userId).orElseThrow();

        Assertions.assertEquals(expectedScore, session.score());
        Assertions.assertEquals(CheckinStep.NOTE, session.step());
    }


    /**
     * Проверяет, что команда /approve работает независимо от регистра и пробелов.
     */
    @Test
    void testHandleApproveIgnoresCaseAndWhitespace() {
        service.start(userId);
        service.handle(userId, "8");
        service.handle(userId, "заметка");

        String response = service.handle(userId, "  /APPROVE  ");

        Assertions.assertEquals("✅ Запись сохранена.", response);
        Assertions.assertEquals(
                1,
                moodRepository.findLast(userId, 10).size()
        );
        Assertions.assertFalse(sessionStore.hasSession(userId));
    }

    /**
     * Проверяет, что команда /reject работает независимо от регистра.
     */
    @Test
    void testHandleRejectIgnoresCase() {
        service.start(userId);
        service.handle(userId, "8");
        service.handle(userId, "заметка");

        String response = service.handle(userId, "/ReJeCt");

        Assertions.assertEquals(
                "Сессия завершена без сохранения отметки",
                response
        );
        Assertions.assertTrue(
                moodRepository.findLast(userId, 10).isEmpty()
        );
        Assertions.assertFalse(sessionStore.hasSession(userId));
    }

    /**
     * Проверяет, что глобальные команды /back и /exit работают независимо от регистра.
     */
    @Test
    void testHandleBackAndExitIgnoreCase() {
        service.start(userId);
        service.handle(userId, "8");

        String backResponse = service.handle(userId, "/Back");
        Assertions.assertEquals(
                "Введите оценку от 1 до 10",
                backResponse
        );
        Assertions.assertEquals(
                CheckinStep.SCORE,
                sessionStore.findById(userId).orElseThrow().step()
        );

        String exitResponse = service.handle(userId, "  /EXIT  ");
        Assertions.assertEquals(
                "Сессия завершена без сохранения отметки",
                exitResponse
        );
        Assertions.assertFalse(sessionStore.hasSession(userId));
    }

    /**
     * Проверяет, что возврат через /back позволяет изменить оценку и заметку перед сохранением.
     */
    @Test
    void testHandleEditScoreAndNoteViaBackSavesUpdatedEntry() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "/back");
        service.handle(userId, "9");
        service.handle(userId, "старая заметка");
        service.handle(userId, "/back");
        service.handle(userId, "новая заметка");

        String response = service.handle(userId, "/approve");

        Assertions.assertEquals("✅ Запись сохранена.", response);
        Assertions.assertFalse(sessionStore.hasSession(userId));

        List<MoodEntry> entries = moodRepository.findLast(userId, 10);
        Assertions.assertEquals(1, entries.size());
        MoodEntry saved = entries.getFirst();
        Assertions.assertEquals(9, saved.score());
        Assertions.assertEquals("новая заметка", saved.note());
    }

    /**
     * Проверяет, что передача {@code null} вместо хранилища сессий вызывает {@link NullPointerException}.
     */
    @Test
    void testConstructorRejectsNullSessionStore() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new CheckinFsmService(null, moodRepository, Clock.systemUTC())
        );

        Assertions.assertEquals(
                "sessionStore не может быть null",
                exception.getMessage()
        );
    }

    /**
     * Проверяет, что передача {@code null} вместо репозитория вызывает {@link NullPointerException}.
     */
    @Test
    void testConstructorRejectsNullMoodRepository() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new CheckinFsmService(sessionStore, null, Clock.systemUTC())
        );

        Assertions.assertEquals(
                "moodRepository не может быть null",
                exception.getMessage()
        );
    }

    /**
     * Проверяет, что передача {@code null} вместо часов вызывает {@link NullPointerException}.
     */
    @Test
    void testConstructorRejectsNullClock() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> new CheckinFsmService(sessionStore, moodRepository, null)
        );

        Assertions.assertEquals(
                "clock не может быть null",
                exception.getMessage()
        );
    }

    /**
     * Проверяет, что старт диалога с {@code null}-идентификатором вызывает {@link NullPointerException}.
     */
    @Test
    void testStartRejectsNullUserId() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> service.start(null)
        );

        Assertions.assertEquals("userId не может быть null", exception.getMessage());
    }

    /**
     * Проверяет, что обработка сообщения с {@code null}-идентификатором вызывает {@link NullPointerException}.
     */
    @Test
    void testHandleRejectsNullUserId() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> service.handle(null, "7")
        );

        Assertions.assertEquals("userId не может быть null", exception.getMessage());
    }

    /**
     * Проверяет, что проверка активной сессии с {@code null}-идентификатором вызывает {@link NullPointerException}.
     */
    @Test
    void testHasActiveSessionRejectsNullUserId() {
        NullPointerException exception = Assertions.assertThrows(
                NullPointerException.class,
                () -> service.hasActiveSession(null)
        );

        Assertions.assertEquals(
                "userId не может быть null",
                exception.getMessage()
        );
    }
}
