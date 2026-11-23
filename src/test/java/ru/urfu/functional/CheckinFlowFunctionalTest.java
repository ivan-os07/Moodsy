package ru.urfu.functional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.commands.BotResponse;
import ru.urfu.commands.CheckinCommand;
import ru.urfu.commands.CommandDispatcher;
import ru.urfu.commands.CommandRegistry;
import ru.urfu.commands.HistoryCommand;
import ru.urfu.model.MoodEntry;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.fsm.checkin.CheckinFsmService;
import ru.urfu.service.fsm.checkin.InMemoryCheckinSessionStore;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Функциональный тест полного сценария чекина.
 * Тест собирает реальную композицию бота без транспортного слоя:
 * диспетчер + FSM + репозитории. Роль транспорта — передача текста в диспетчер.
 */
class CheckinFlowFunctionalTest {

    /**
     * Диспетчер: принимает текст пользователя, как это делает транспорт.
     */
    private CommandDispatcher dispatcher;

    /**
     * Репозиторий записей: по нему проверяем, что чекин действительно сохранился.
     */
    private InMemoryMoodRepository moodRepository;

    /**
     * Внутренний идентификатор пользователя, проходящего чекин.
     */
    private final UUID userId = UUID.randomUUID();

    /**
     * Собирает бота целиком: реальные репозитории, фиксированное время,
     * зарегистрированные команды — как в Main, но без транспорта.
     */
    @BeforeEach
    void setUp() {
        InMemoryCheckinSessionStore sessionStore = new InMemoryCheckinSessionStore();
        moodRepository = new InMemoryMoodRepository();
        CheckinFsmService checkinFsmService = new CheckinFsmService(sessionStore,
                moodRepository,
                Clock.fixed(Instant.parse("2026-11-02T09:00:00Z"), ZoneOffset.UTC)
        );
        // Регистрируем команды в хранилище, как это делает Main
        CommandRegistry registry = new CommandRegistry();
        registry.register(new CheckinCommand(checkinFsmService));
        registry.register(new HistoryCommand(moodRepository));
        dispatcher = new CommandDispatcher(registry, checkinFsmService);
    }

    /**
     * Полный успешный сценарий с заметкой.
     * Пользователь: /checkin → 7 → "дела" → "много тикетов" → /approve → /history.
     * Проверяются: ответы бота, факт сохранения записи и её вид в истории.
     */
    @Test
    void testHappyPathWithNote() {
        // подаём сообщения пользователя
        BotResponse first = dispatcher.handle(userId, "/checkin");
        BotResponse score = dispatcher.handle(userId, "7");
        BotResponse sphere = dispatcher.handle(userId, "дела");
        BotResponse note = dispatcher.handle(userId, "много тикетов");
        BotResponse approve = dispatcher.handle(userId, "/approve");

        // ответы совпадают с диалогом
        Assertions.assertEquals("Введите оценку от 1 до 10", first.text());
        Assertions.assertEquals("Выберете сферу из предложенных:\nДела, Общение, Здоровье, Отдых\n", score.text());
        Assertions.assertEquals("Добавьте заметку или отправьте /skip", sphere.text());
        Assertions.assertEquals("""
                📋 Предпросмотр карточки:
                • Оценка: 7/10
                • Сфера: Дела
                • Заметка: много тикетов
                Подтвердите запись:
                /approve — сохранить
                /reject — отменить
                """, note.text()
        );
        Assertions.assertEquals("✅ Запись сохранена.", approve.text());

        // запись появилась в репозитории с нужными данными
        MoodEntry saved = moodRepository.findLast(userId, 1).getFirst();
        Assertions.assertEquals(7, saved.score());
        Assertions.assertEquals("Дела", saved.sphere().getDisplayName());
        Assertions.assertEquals("много тикетов", saved.note());

        // /history показывает запись (09:00 UTC = 12:00 по Мск)
        BotResponse history = dispatcher.handle(userId, "/history");

        Assertions.assertEquals("""
                 📅 Ваша история записей:
                02.11.2026
                12:00 - 7/10 - Дела
                Заметка: много тикетов""", history.text()
        );
    }

    /**
     * Полный успешный сценарий без заметки.
     * Пользователь: /checkin → 8 → "дела" → /skip → /approve → /history.
     * Проверяются: ответы бота, факт сохранения записи и её вид в истории.
     */
    @Test
    void testHappyPathWithoutNoteUsingSkip() {
        // подаём сообщения пользователя
        BotResponse first = dispatcher.handle(userId, "/checkin");
        BotResponse score = dispatcher.handle(userId, "8");
        BotResponse sphere = dispatcher.handle(userId, "дела");
        BotResponse note = dispatcher.handle(userId, "/skip");
        BotResponse approve = dispatcher.handle(userId, "/approve");

        // ответы совпадают с диалогом
        Assertions.assertEquals("Введите оценку от 1 до 10", first.text());
        Assertions.assertEquals("Выберете сферу из предложенных:\nДела, Общение, Здоровье, Отдых\n", score.text());
        Assertions.assertEquals("Добавьте заметку или отправьте /skip", sphere.text());

        Assertions.assertEquals("""
                📋 Предпросмотр карточки:
                • Оценка: 8/10
                • Сфера: Дела
                • Заметка:
                Подтвердите запись:
                /approve — сохранить
                /reject — отменить
                """, note.text()
        );

        Assertions.assertEquals("✅ Запись сохранена.", approve.text());

        // запись появилась в репозитории с нужными данными
        MoodEntry saved = moodRepository.findLast(userId, 1).getFirst();
        Assertions.assertEquals(8, saved.score());
        Assertions.assertEquals("Дела", saved.sphere().getDisplayName());
        Assertions.assertEquals("", saved.note());

        // /history показывает запись (09:00 UTC = 12:00 по Мск)
        BotResponse history = dispatcher.handle(userId, "/history");

        Assertions.assertEquals("""
                 📅 Ваша история записей:
                02.11.2026
                12:00 - 8/10 - Дела""", history.text()
        );

    }


    /**
     * Сценарий выхода из диалога создания сферы.
     * Пользователь: /checkin → 1 → /exit.
     * Проверяются: ответы бота, факт несохранения записи и не появления её в истории.
     */
    @Test
    void testExitFromSpherePath() {
        // подаём сообщения пользователя
        BotResponse first = dispatcher.handle(userId, "/checkin");
        BotResponse score = dispatcher.handle(userId, "1");
        BotResponse exit = dispatcher.handle(userId, "/exit");

        // ответы совпадают с диалогом
        Assertions.assertEquals("Введите оценку от 1 до 10", first.text());
        Assertions.assertEquals("Выберете сферу из предложенных:\nДела, Общение, Здоровье, Отдых\n", score.text());
        Assertions.assertEquals("Сессия завершена без сохранения отметки", exit.text());

        // запись не появилась в репозитории
        Assertions.assertTrue(moodRepository.findLast(userId, 1).isEmpty());

        // /history подсказывает, что заметок нет
        BotResponse history = dispatcher.handle(userId, "/history");
        Assertions.assertEquals("История пуста. Попробуйте использовать команду /checkin", history.text());

    }

    /**
     * Сценарий использования старого варианта чекин (с параметрами).
     * Пользователь: /checkin 1.
     * Проверяются: ответы бота (сообщение об ошибке), факт несохранения записи и не появления её в истории.
     */
    @Test
    void testPathUsingArgs() {
        // подаём сообщения пользователя
        BotResponse first = dispatcher.handle(userId, "/checkin 1");

        // ответы совпадают с диалогом
        Assertions.assertEquals("❌ Команда /checkin вызывается без параметров.", first.text());

        // запись не появилась в репозитории
        Assertions.assertTrue(moodRepository.findLast(userId, 1).isEmpty());

        // /history подсказывает, что заметок нет
        BotResponse history = dispatcher.handle(userId, "/history");
        Assertions.assertEquals("История пуста. Попробуйте использовать команду /checkin", history.text());
    }

    /**
     * Сценарий использования команды /back. Сначала идем до шага PREVIEW,
     * а потом возвращаемся назад, пытаясь применить команду /back на первом шаге.
     * Пользователь: /checkin → 8 → дела → много тикетов → /back → /back → /back → /back → /exit → /history.
     * Проверяются: ответы бота (сообщение об ошибке), факт несохранения записи и не появления её в истории.
     */
    @Test
    void testCheckinShouldNotSaveEntryWhenUserCancels() {
        // подаём сообщения пользователя
        dispatcher.handle(userId, "/checkin");
        dispatcher.handle(userId, "7");
        dispatcher.handle(userId, "дела");
        dispatcher.handle(userId, "много тикетов");

        BotResponse backToNote = dispatcher.handle(userId, "/back");
        BotResponse backToSphere = dispatcher.handle(userId, "/back");
        BotResponse backToScore = dispatcher.handle(userId, "/back");
        BotResponse backWrong = dispatcher.handle(userId, "/back");

        // ответы совпадают с диалогом
        Assertions.assertEquals("Добавьте заметку или отправьте /skip", backToNote.text());
        Assertions.assertEquals("Выберете сферу из предложенных:\nДела, Общение, Здоровье, Отдых\n",
                backToSphere.text()
        );
        Assertions.assertEquals("Введите оценку от 1 до 10", backToScore.text());
        Assertions.assertEquals("❌ Вы на первом шаге, вернуться назад нельзя.", backWrong.text());


        dispatcher.handle(userId, "/exit");

        // запись не появилась в репозитории
        Assertions.assertTrue(moodRepository.findLast(userId, 1).isEmpty());

        // /history подсказывает, что заметок нет
        BotResponse history = dispatcher.handle(userId, "/history");
        Assertions.assertEquals("История пуста. Попробуйте использовать команду /checkin", history.text());

    }

    /**
     * Проверяет, что параллельные диалоги двух пользователей не влияют друг на друга,
     * а их записи в истории остаются изолированными.
     */
    @Test
    void testParallelDialogsOfTwoUsersAreIsolated() {
        UUID secondUserId = UUID.randomUUID();

        dispatcher.handle(userId, "/checkin");
        dispatcher.handle(secondUserId, "/checkin");

        dispatcher.handle(userId, "9");
        dispatcher.handle(secondUserId, "3");

        dispatcher.handle(userId, "дела");
        dispatcher.handle(secondUserId, "отдых");

        dispatcher.handle(userId, "отличный день");
        dispatcher.handle(secondUserId, "/exit");

        BotResponse approveFirst = dispatcher.handle(userId, "/approve");

        Assertions.assertEquals("✅ Запись сохранена.", approveFirst.text());

        BotResponse firstHistory = dispatcher.handle(userId, "/history");

        Assertions.assertEquals("""
                 📅 Ваша история записей:
                02.11.2026
                12:00 - 9/10 - Дела
                Заметка: отличный день""", firstHistory.text()
        );

        BotResponse secondHistory = dispatcher.handle(secondUserId, "/history");

        Assertions.assertEquals(
                "История пуста. Попробуйте использовать команду /checkin",
                secondHistory.text()
        );
    }

}
