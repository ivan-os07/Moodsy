package ru.urfu.service.fsm;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import ru.urfu.model.MoodEntry;
import ru.urfu.model.Sphere;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.fsm.checkin.CheckinFsmService;
import ru.urfu.service.fsm.checkin.CheckinSession;
import ru.urfu.service.fsm.checkin.CheckinStep;
import ru.urfu.service.fsm.checkin.InMemoryCheckinSessionStore;

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
    void start_createsSessionAndAsksForScoreTest() {
        String response = service.start(userId);

        Assertions.assertEquals("Введите оценку от 1 до 10", response);
        Assertions.assertTrue(sessionStore.hasSession(userId));
        Assertions.assertEquals(CheckinStep.SCORE, sessionStore.findById(userId).orElseThrow().step());
    }

    /**
     * Проверяет открыта ли сессия у пользователя.
     */
    @Test
    void hasActiveSessionTest() {
        Assertions.assertFalse(service.hasActiveSession(userId));

        service.start(userId);

        Assertions.assertTrue(service.hasActiveSession(userId));
    }

    /**
     * Ввод оценки на шаге SCORE (переход к SPHERE)
     * Пользователь начал чекин и отправляет число, например "7".
     */
    @Test
    void handle_validScore_savesScoreAndAsksForSphereTest() {
        service.start(userId);

        String response = service.handle(userId, "7");

        Assertions.assertEquals("""
                Выберете сферу из предложенных:
                Дела, Общение, Здоровье, Отдых
                """, response
        );

        CheckinSession session = sessionStore.findById(userId).orElseThrow();

        Assertions.assertEquals(7, session.score());
        Assertions.assertEquals(CheckinStep.SPHERE, session.step());
    }

    /**
     * Ввод невалидного значения(строки) на шаге SCORE → ошибка, остаёмся на SCORE
     * Пользователь начал чекин и отправляет строку, например "abc".
     */
    @Test
    void handle_invalidScoreStringAndStayOnScoreTest() {
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
    void handle_invalidScoreIntAndStayOnScoreTest() {
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
    void handle_validNoteTest() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        // Успешный пройденный шаг SPHERE
        service.handle(userId, "Дела");

        String response = service.handle(userId, "очень много тикетов");

        String expected = """
                📋 Предпросмотр карточки:
                • Оценка: 7/10
                • Сфера: Дела
                • Заметка: очень много тикетов
                Подтвердите запись:
                /approve — сохранить
                /reject — отменить
                """;
        Assertions.assertEquals(expected, response);

        // Переходим в PREVIEW
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Успешный ввод пустой строки(=команде skip) на шаге NOTE → пустая заметка, переход в PREVIEW
     */
    @Test
    void handle_validEmptyNoteTest() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        // Успешный пройденный шаг SPHERE
        service.handle(userId, "Дела");

        String response = service.handle(userId, "");

        String expected = """
                \uD83D\uDCCB Предпросмотр карточки:
                • Оценка: 7/10
                • Сфера: Дела
                • Заметка:
                Подтвердите запись:
                /approve — сохранить
                /reject — отменить
                """;
        Assertions.assertEquals(expected, response);

        // Переходим в PREVIEW
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Успешный ввод команды skip на шаге NOTE → пустая заметка, переход в PREVIEW
     */
    @Test
    void handle_validCmdSkipTest() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        // Успешный пройденный шаг SPHERE
        service.handle(userId, "Дела");

        String response = service.handle(userId, "/skip");

        String expected = """
                \uD83D\uDCCB Предпросмотр карточки:
                • Оценка: 7/10
                • Сфера: Дела
                • Заметка:
                Подтвердите запись:
                /approve — сохранить
                /reject — отменить
                """;
        Assertions.assertEquals(expected, response);

        // Переходим в PREVIEW
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Неуспешный ввод команды skip на шаге не NOTE, и не на PREVIEW → сообщение об ошибке
     */
    @Test
    void handle_invalidCmdSkipNotPreviewTest() {
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
    void handle_invalidCmdSkipPreviewTest() {
        String expected = """
                ❌ Ошибка: команда /skip доступна только при вводе заметки.
                Пожалуйста, подтвердите запись с помощью /approve или отмените через /reject.""";

        // Идем к шагу PREVIEW
        service.start(userId);
        service.handle(userId, "1");
        service.handle(userId, "Дела");
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
    void handleValidSaveTest() {
        service.start(userId);
        // Успешный пройденный шаг SCORE
        service.handle(userId, "7");

        // Успешный пройденный шаг SPHERE
        service.handle(userId, "Дела");

        // Успешный пройденный шаг NOTE
        service.handle(userId, "заметка");

        String response = service.handle(userId, "/approve");
        Assertions.assertEquals("✅ Запись сохранена.", response);

        // Проверяем, что запись появилась в репозитории
        MoodEntry savedEntry = moodRepository.findLatest(userId).orElseThrow();
        Assertions.assertEquals(7, savedEntry.score());
        Assertions.assertEquals(Sphere.WORK, savedEntry.sphere());
        Assertions.assertEquals("заметка", savedEntry.note());
        Assertions.assertEquals(LocalDateTime.now(clock.withZone(ZoneOffset.ofHours(3))), savedEntry.createdAt());

        // Сессия закрылась
        Assertions.assertFalse(service.hasActiveSession(userId));
    }

    /**
     * Выход из диалога по команде /exit: сессия удаляется, запись не сохраняется.
     */
    @Test
    void handle_exitCancelsSessionWithoutSavingTest() {
        service.start(userId);
        service.handle(userId, "7"); // находимся на шаге SPHERE

        String response = service.handle(userId, "/exit");

        Assertions.assertEquals("Сессия завершена без сохранения отметки", response);
        Assertions.assertFalse(sessionStore.hasSession(userId));
        Assertions.assertTrue(moodRepository.findLatest(userId).isEmpty());
    }

    /**
     * Отмена записи по команде /reject на шаге PREVIEW: сессия удаляется, запись не сохраняется.
     */
    @Test
    void handle_rejectCancelsSessionWithoutSavingTest() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "Дела");
        service.handle(userId, "заметка");

        String response = service.handle(userId, "/reject");

        Assertions.assertEquals("Сессия завершена без сохранения отметки", response);
        Assertions.assertFalse(sessionStore.hasSession(userId));
        Assertions.assertTrue(moodRepository.findLatest(userId).isEmpty());
    }

    /**
     * Возврат назад с шага NOTE на шаг SCORE по команде /back.
     */
    @Test
    void handle_backFromNoteToScoreTest() {
        service.start(userId);
        service.handle(userId, "7"); // перешли на SPHERE
        service.handle(userId, "Дела"); // перешли на NOTE

        service.handle(userId, "/back");
        String response = service.handle(userId, "/back");

        Assertions.assertEquals("Введите оценку от 1 до 10", response);
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SCORE, session.step());
    }

    /**
     * Возврат назад с шага PREVIEW на шаг NOTE по команде /back.
     */
    @Test
    void handle_backFromPreviewToNoteTest() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "Дела");
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
    void handle_backFromScoreReturnsErrorTest() {
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
    void handle_invalidInputOnPreviewTest() {
        service.start(userId);
        service.handle(userId, "7");
        service.handle(userId, "Дела");
        service.handle(userId, "заметка");

        String response = service.handle(userId, "что-то другое");

        Assertions.assertEquals("Пожалуйста, подтвердите запись с помощью /approve или отмените через /reject.",
                response
        );
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.PREVIEW, session.step());
    }

    /**
     * Успешный ввод каждой валидной сферы на шаге SPHERE → переход к NOTE.
     */
    @Test
    void handle_validSphereTransitionsToNoteTest() {
        List<String> spheres = List.of("Дела", "Общение", "Здоровье", "Отдых");

        // Проверяем все 4 сферы
        for (String sp : spheres) {
            service.start(userId);
            service.handle(userId, "7");

            String response = service.handle(userId, sp);

            Assertions.assertEquals("Добавьте заметку или отправьте /skip", response);
            CheckinSession session = sessionStore.findById(userId).orElseThrow();
            Assertions.assertEquals(CheckinStep.NOTE, session.step());
            Assertions.assertEquals(sp, session.sphere().getDisplayName());
            Assertions.assertEquals(7, session.score());

            // Сбрасываем сессию для следующей итерации
            sessionStore.remove(userId);
        }
    }

    /**
     * Команда /exit на шаге SPHERE → сессия удаляется без сохранения.
     */
    @Test
    void handle_exitOnSphereCancelsSessionTest() {
        service.start(userId);
        service.handle(userId, "5"); // SCORE → SPHERE

        String response = service.handle(userId, "/exit");

        Assertions.assertEquals("Сессия завершена без сохранения отметки", response);
        Assertions.assertFalse(sessionStore.hasSession(userId));
        Assertions.assertTrue(moodRepository.findLatest(userId).isEmpty());
    }

    /**
     * Команда /back на шаге SPHERE → возврат на SCORE, оценка сохраняется.
     */
    @Test
    void handle_backFromSphereToScoreTest() {
        service.start(userId);
        service.handle(userId, "5");

        String response = service.handle(userId, "/back");

        Assertions.assertEquals("Введите оценку от 1 до 10", response);
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SCORE, session.step());
        Assertions.assertEquals(5, session.score());
        Assertions.assertNull(session.sphere());
    }

    /**
     * Команда /skip на шаге SPHERE → ошибка, остаёмся на SPHERE.
     */
    @Test
    void handle_skipOnSphereReturnsErrorTest() {
        service.start(userId);
        service.handle(userId, "5"); // SCORE → SPHERE

        String response = service.handle(userId, "/skip");

        Assertions.assertEquals("❌ Ошибка: команда /skip доступна только при вводе заметки.", response);
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SPHERE, session.step());
    }

    /**
     * Команды /approve и /reject на шаге SPHERE → ошибка неизвестной команды, остаёмся на SPHERE.
     */
    @Test
    void handle_approveRejectOnSphereReturnsUnknownCommandTest() {
        service.start(userId);
        service.handle(userId, "5"); // SCORE → SPHERE

        Assertions.assertEquals("❌ Неизвестная команда. Доступны: /back, /exit",
                service.handle(userId, "/approve")
        );
        Assertions.assertEquals("❌ Неизвестная команда. Доступны: /back, /exit",
                service.handle(userId, "/reject")
        );

        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.SPHERE, session.step());
    }


    /**
     * Ввод сферы с пробелами и в разном регистре на шаге SPHERE переводит на шаг NOTE.
     */
    @ParameterizedTest
    @ValueSource(strings = {
            "Дела",
            "дела",
            "ДЕЛА",
            "дЕлА",
            " Дела ",
            "   дела   "
    })
    void handle_validSphereWithWhitespaceAndCaseTransitionsToNoteTest(String sphereInput) {
        service.start(userId);
        service.handle(userId, "5");

        String response = service.handle(userId, sphereInput);

        Assertions.assertEquals("Добавьте заметку или отправьте /skip", response);
        CheckinSession session = sessionStore.findById(userId).orElseThrow();
        Assertions.assertEquals(CheckinStep.NOTE, session.step());
    }
}
