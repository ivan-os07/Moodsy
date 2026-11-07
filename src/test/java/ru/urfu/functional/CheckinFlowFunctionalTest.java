package ru.urfu.functional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.urfu.commands.BotResponse;
import ru.urfu.commands.CheckinCommand;
import ru.urfu.commands.CommandDispatcher;
import ru.urfu.commands.HistoryCommand;
import ru.urfu.model.MoodEntry;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.service.fsm.CheckinFsmService;
import ru.urfu.service.fsm.InMemoryCheckinSessionStore;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
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
        CheckinFsmService checkinFsmService = new CheckinFsmService(sessionStore, moodRepository, Clock.fixed(Instant.parse("2026-11-02T09:00:00Z"), ZoneOffset.UTC));
        dispatcher = new CommandDispatcher(checkinFsmService);
        dispatcher.register(new CheckinCommand(checkinFsmService));
        dispatcher.register(new HistoryCommand(moodRepository));
    }

    /**
     * Полный успешный сценарий с заметкой.
     * Пользователь: /checkin → 7 → "много тикетов" → /approve → /history.
     * Проверяются: ответы бота, факт сохранения записи и её вид в истории.
     */
    @Test
    void happyPathWithNote() {
        // подаём сообщения пользователя
        BotResponse first = dispatcher.handle(userId, "/checkin");
        BotResponse score = dispatcher.handle(userId, "7");
        BotResponse note = dispatcher.handle(userId, "много тикетов");
        BotResponse approve = dispatcher.handle(userId, "/approve");

        // ответы совпадают с диалогом
        Assertions.assertEquals("Введите оценку от 1 до 10", first.text());
        Assertions.assertEquals("Добавьте заметку или отправьте /skip", score.text());
        Assertions.assertTrue(note.text().contains("Предпросмотр карточки:"));
        Assertions.assertTrue(note.text().contains("Оценка: 7/10"));
        Assertions.assertEquals("✅ Запись сохранена.", approve.text());

        // запись появилась в репозитории с нужными данными
        MoodEntry saved = moodRepository.findLatest(userId).orElseThrow();
        Assertions.assertEquals(7, saved.score());
        Assertions.assertEquals("много тикетов", saved.note());

        // /history показывает запись (09:00 UTC = 12:00 по Мск)
        String history = dispatcher.handle(userId, "/history").text();
        Assertions.assertTrue(history.contains("02.11.2026 12:00 - 7/10"));
        Assertions.assertTrue(history.contains("Заметка: много тикетов"));
    }

    /**
     * Полный успешный сценарий без заметки.
     * Пользователь: /checkin → 8 → /skip → /approve → /history.
     * Проверяются: ответы бота, факт сохранения записи и её вид в истории.
     */
    @Test
    void happyPathWithoutNoteUsingSkip() {
        // подаём сообщения пользователя
        BotResponse first = dispatcher.handle(userId, "/checkin");
        BotResponse score = dispatcher.handle(userId, "8");
        BotResponse note = dispatcher.handle(userId, "/skip");
        BotResponse approve = dispatcher.handle(userId, "/approve");

        // ответы совпадают с диалогом
        Assertions.assertEquals("Введите оценку от 1 до 10", first.text());
        Assertions.assertEquals("Добавьте заметку или отправьте /skip", score.text());
        Assertions.assertTrue(note.text().contains("Предпросмотр карточки:"));
        Assertions.assertTrue(note.text().contains("Оценка: 8/10"));
        Assertions.assertEquals("✅ Запись сохранена.", approve.text());

        // запись появилась в репозитории с нужными данными
        MoodEntry saved = moodRepository.findLatest(userId).orElseThrow();
        Assertions.assertEquals(8, saved.score());
        Assertions.assertEquals("", saved.note());

        // /history показывает запись (09:00 UTC = 12:00 по Мск)
        String history = dispatcher.handle(userId, "/history").text();
        Assertions.assertTrue(history.contains("02.11.2026 12:00 - 8/10"));
        Assertions.assertFalse(history.contains("Заметка: "));

    }


    /**
     * Сценарий выхода из диалога создания заметки.
     * Пользователь: /checkin → 1 → /exit.
     * Проверяются: ответы бота, факт несохранения записи и не появления её в истории.
     */
    @Test
    void exitPath() {
        // подаём сообщения пользователя
        BotResponse first = dispatcher.handle(userId, "/checkin");
        BotResponse score = dispatcher.handle(userId, "1");
        BotResponse exit = dispatcher.handle(userId, "/exit");

        // ответы совпадают с диалогом
        Assertions.assertEquals("Введите оценку от 1 до 10", first.text());
        Assertions.assertEquals("Добавьте заметку или отправьте /skip", score.text());
        Assertions.assertTrue(exit.text().contains("Сессия завершена без сохранения отметки"));

        // запись не появилась в репозитории
        Optional<MoodEntry> saved = moodRepository.findLatest(userId);
        Assertions.assertTrue(saved.isEmpty());

        // /history подсказывает, что заметок нет
        String history = dispatcher.handle(userId, "/history").text();
        Assertions.assertTrue(history.contains("История пуста. Попробуйте использовать команду /checkin"));
        Assertions.assertFalse(history.contains("Заметка: "));
    }

    /**
     * Сценарий использования старого варианта чекин (с параметрами).
     * Пользователь: /checkin 1.
     * Проверяются: ответы бота (сообщение об ошибке), факт несохранения записи и не появления её в истории.
     */
    @Test
    void pathUsingArgs() {
        // подаём сообщения пользователя
        BotResponse first = dispatcher.handle(userId, "/checkin 1");

        // ответы совпадают с диалогом
        Assertions.assertEquals("❌ Команда /checkin вызывается без параметров.", first.text());

        // запись не появилась в репозитории
        Optional<MoodEntry> saved = moodRepository.findLatest(userId);
        Assertions.assertTrue(saved.isEmpty());

        // /history подсказывает, что заметок нет
        String history = dispatcher.handle(userId, "/history").text();
        Assertions.assertTrue(history.contains("История пуста. Попробуйте использовать команду /checkin"));
        Assertions.assertFalse(history.contains("Заметка: "));
    }

    /**
     * Сценарий использования команды /back. Сначала идем до шага PREVIEW,
     * а потом возвращаемся назад, пытаясь применить команду /back на первом шаге.
     * Пользователь: /checkin → 8 → много тикетов → /back → /back → /back → /exit → /history.
     * Проверяются: ответы бота (сообщение об ошибке), факт несохранения записи и не появления её в истории.
     */
    @Test
    void checkinShouldNotSaveEntryWhenUserCancels() {
        // подаём сообщения пользователя
        dispatcher.handle(userId, "/checkin");
        dispatcher.handle(userId, "7");
        dispatcher.handle(userId, "много тикетов");

        BotResponse backToNote = dispatcher.handle(userId, "/back");
        BotResponse backToScore = dispatcher.handle(userId, "/back");
        BotResponse backWrong = dispatcher.handle(userId, "/back");

        // ответы совпадают с диалогом
        Assertions.assertEquals("Добавьте заметку или отправьте /skip", backToNote.text());
        Assertions.assertEquals("Введите оценку от 1 до 10", backToScore.text());
        Assertions.assertEquals("❌ Вы на первом шаге, вернуться назад нельзя.", backWrong.text());


        dispatcher.handle(userId, "/exit");

        // запись не появилась в репозитории
        Optional<MoodEntry> saved = moodRepository.findLatest(userId);
        Assertions.assertTrue(saved.isEmpty());

        // /history подсказывает, что заметок нет
        String history = dispatcher.handle(userId, "/history").text();
        Assertions.assertTrue(history.contains("История пуста. Попробуйте использовать команду /checkin"));
        Assertions.assertFalse(history.contains("Заметка: "));
    }

}
