package ru.urfu.service.fsm;

import ru.urfu.model.MoodEntry;
import ru.urfu.repository.MoodRepository;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Управляет пошаговым диалогом чекина (управляет сессией):
 * <ul>
 *     <li>Проверяет ввод</li>
 *     <li>Переводит пользователя между шагами</li>
 *     <li>Сохраняет готовую запись в репозиторий</li>
 * </ul>
 */
public class CheckinFsmService {

    /**
     * Команда завершения диалога без сохранения.
     */
    private static final String CMD_EXIT = "/exit";

    /**
     * Команда возврата на предыдущий шаг.
     */
    private static final String CMD_BACK = "/back";

    /**
     * Команда пропуска заметки.
     */
    private static final String CMD_SKIP = "/skip";

    /**
     * Команда сохранения записи.
     */
    private static final String CMD_APPROVE = "/approve";

    /**
     * Команда отмены записи.
     */
    private static final String CMD_REJECT = "/reject";

    /**
     * Минимально допустимая оценка настроения.
     */
    private static final int MIN_SCORE = 1;

    /**
     * Максимально допустимая оценка настроения.
     */
    private static final int MAX_SCORE = 10;

    /**
     * Приглашение ввести оценку.
     */
    private static final String SCORE_PROMPT = "Введите оценку от 1 до 10";

    /**
     * Приглашение ввести заметку.
     */
    private static final String NOTE_PROMPT = "Добавьте заметку или отправьте /skip";

    /**
     * Сообщение об ошибке оценки.
     */
    private static final String SCORE_ERROR = "❌ Ошибка: оценка настроения должна быть числом от 1 до 10.";

    /**
     * Сообщение о неизвестной команде на шаге заметки.
     */
    private static final String UNKNOWN_COMMAND = "❌ Неизвестная команда. Доступны: /back, /exit";

    /**
     * Сообщение об ошибке при вызове команды /skip не на шаге заметки.
     */
    private static final String SKIP_ERROR = "❌ Ошибка: команда /skip доступна только при вводе заметки.";


    /**
     * Сообщение о невозможности вернуться назад с первого шага.
     */
    private static final String FIRST_STEP = "❌ Вы на первом шаге, вернуться назад нельзя.";

    /**
     * Подсказка о доступных действиях на шаге предпросмотра.
     */
    private static final String PREVIEW_HINT = "Пожалуйста, подтвердите запись с помощью /approve или отмените через /reject.";

    /**
     * Сообщение о попытке продолжить диалог, которого нет.
     */
    private static final String NO_SESSION = "Диалог не запущен. Отправьте /checkin";

    /**
     * Сообщение об успешном сохранении записи.
     */
    private static final String SAVED = "✅ Запись сохранена.";

    /**
     * Сообщение о завершении диалога без сохранения.
     */
    private static final String EXITED = "Сессия завершена без сохранения отметки";

    /**
     * Хранилище открытых сессий диалогов.
     */
    private final CheckinSessionStore sessionStore;

    /**
     * Репозиторий записей настроения.
     */
    private final MoodRepository moodRepository;

    /**
     * Источник времени, приведённый к московскому (UTC+3).
     */
    private final Clock clock;

    /**
     * Создаёт сервис диалога чекина.
     *
     * @param sessionStore   хранилище открытых сессий
     * @param moodRepository репозиторий записей настроения
     * @param clock          источник времени; время приводится к московскому (UTC+3)
     */
    public CheckinFsmService(CheckinSessionStore sessionStore, MoodRepository moodRepository, Clock clock) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore не может быть null");
        this.moodRepository = Objects.requireNonNull(moodRepository, "moodRepository не может быть null");
        this.clock = Objects.requireNonNull(clock, "clock не может быть null").withZone(ZoneOffset.ofHours(3));
    }

    /**
     * Создаёт сессию на шаге SCORE и просит ввести оценку.
     * Если сессия уже была создана, то старая удалится и новая добавится.
     *
     * @param userId идентификатор пользователя
     * @return приглашение ввести оценку
     */
    public String start(UUID userId) {
        CheckinSession session = new CheckinSession(userId, CheckinStep.SCORE, 0, "");
        sessionStore.save(session);
        return SCORE_PROMPT;
    }

    /**
     * Обрабатывает ввод пользователя в зависимости от текущего шага диалога.
     * Команды /exit и /back доступны на любом шаге.
     *
     * @param userId идентификатор пользователя
     * @param text   текст сообщения
     * @return сообщение с результатом обработки
     */
    public String handle(UUID userId, String text) {
        Objects.requireNonNull(userId, "userId не может быть null");

        // Проверяем наличие сессии
        if (!sessionStore.hasSession(userId)) {
            return NO_SESSION;
        }

        Optional<CheckinSession> found = sessionStore.findById(userId);
        if (found.isEmpty()) {
            return NO_SESSION;
        }

        CheckinSession session = found.get();
        String input = text == null ? "" : text.trim();
        String command = input.toLowerCase(Locale.ROOT);

        return switch (command) {
            // обработка выхода
            case CMD_EXIT -> handleExit(session);
            case CMD_BACK -> handleBack(session);
            case CMD_SKIP -> handleSkip(session);

            // Идем по шагам
            default -> switch (session.step()) {
                case SCORE -> handleScore(session, input);
                case NOTE -> handleNote(session, input);
                case PREVIEW -> handlePreview(session, input);
            };
        };

    }

    /**
     * На шаге PREVIEW: /approve сохраняет запись, /reject отменяет диалог,
     * остальной ввод повторяет подсказку.
     *
     * @param session текущая сессия
     * @param command ввод пользователя в нижнем регистре
     * @return сообщение по результату
     */
    private String handlePreview(CheckinSession session, String command) {
        if (CMD_APPROVE.equals(command)) {
            return handleApprove(session);
        }
        if (CMD_REJECT.equals(command)) {
            return handleExit(session);
        }
        return PREVIEW_HINT;
    }

    /**
     * Принимает оценку на шаге SCORE и переводит диалог на шаг NOTE.
     *
     * @param session текущая сессия
     * @param text    введённый текст
     * @return приглашение ввести заметку или сообщение об ошибке
     */
    private String handleScore(CheckinSession session, String text) {
        // тк мы приняли сессию, то она точно не пуста (мы проверили ее в handle)
        int score;
        try {
            score = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            // Сессия остается та же самая(с теми же данными)
            return SCORE_ERROR;
        }

        // валидация
        if (score < MIN_SCORE || score > MAX_SCORE) {
            // Сессия остается та же самая(с теми же данными)
            return SCORE_ERROR;
        }

        // Создаем новую сессию
        CheckinSession updatedSession = new CheckinSession(session.userId(), CheckinStep.NOTE,    // меняем шаг на следующий
                score,               // оценка
                session.note());

        // Сохраняем новую сессию в хранилище (она заменит старую по контракту save)
        sessionStore.save(updatedSession);

        return NOTE_PROMPT;
    }

    /**
     * Принимает заметку или пропускает(/skip) её и переводит диалог на шаг PREVIEW.
     *
     * @param session текущая сессия
     * @param text    введённый текст
     * @return текст карточки предпросмотра или сообщение об ошибке
     */
    private String handleNote(CheckinSession session, String text) {
        // тк мы приняли сессию, то она точно не пуста (мы проверили ее в handle)

        // Защита от null и очистка пробелов по краям
        String cleanText = (text == null) ? "" : text.trim();
        String note;

        if (CMD_SKIP.equalsIgnoreCase(cleanText)) {
            note = "";
        } else if (cleanText.startsWith("/")) {
            return UNKNOWN_COMMAND;
        } else {
            note = cleanText;
        }

        // Создаем новую сессию
        CheckinSession updatedSession = new CheckinSession(session.userId(), CheckinStep.PREVIEW, // Меняем шаг на следующий,
                session.score(), note // Новая заметка
        );

        // Сохраняем новую сессию в хранилище (она заменит старую по контракту save)
        // При этом session почистит GC
        sessionStore.save(updatedSession);

        return buildPreview(updatedSession);
    }


    /**
     * Сохраняет запись из предпросмотра и удаляет сессию.
     *
     * @param session текущая сессия
     * @return сообщение о сохранении записи
     */
    private String handleApprove(CheckinSession session) {
        MoodEntry entry = new MoodEntry(LocalDateTime.now(clock), session.score(), session.note());

        moodRepository.save(session.userId(), entry);
        sessionStore.remove(session.userId());
        return SAVED;
    }

    /**
     * Удаляет сессию без сохранения записи.
     *
     * @param session текущая сессия
     * @return сообщение о завершении диалога
     */
    private String handleExit(CheckinSession session) {
        sessionStore.remove(session.userId());
        return EXITED;
    }

    /**
     * Переводит диалог на предыдущий шаг и сообщает, куда он вернулся.
     *
     * @param session текущая сессия
     * @return приглашение предыдущего шага или сообщение о невозможности возврата
     */
    private String handleBack(CheckinSession session) {
        // тк мы приняли сессию, то она точно не пуста (мы проверили ее в handle)

        CheckinSession updatedSession;

        switch (session.step()) {
            case SCORE -> {
                return FIRST_STEP;
            }
            case NOTE -> {
                updatedSession = new CheckinSession(session.userId(), CheckinStep.SCORE, session.score(), session.note());
                sessionStore.save(updatedSession);
                return SCORE_PROMPT;
            }
            case PREVIEW -> {
                updatedSession = new CheckinSession(session.userId(), CheckinStep.NOTE, session.score(), session.note());
                sessionStore.save(updatedSession);
                return NOTE_PROMPT;
            }
        }
        return "Неизвестный шаг: " + session.step();
    }

    /**
     * Обработка команды /skip
     *
     * @param session текущая сессия
     * @return сообщение об ошибке если не на шаге NOTE
     */
    private String handleSkip(CheckinSession session) {
        // тк мы приняли сессию, то она точно не пуста (мы проверили ее в handle)

        CheckinSession updatedSession;

        // Если не на шаге NOTE, то выбрасываем сообщение об ошибке
        if (session.step() != CheckinStep.NOTE) {
            // Если на шаге PREVIEW, то еще напоминаем, какие команды доступны на этом шаге
            if (session.step() == CheckinStep.PREVIEW) {
                return SKIP_ERROR + '\n' + PREVIEW_HINT;
            }
            return SKIP_ERROR;
        }

        // Иначе, пропускаем шаг NOTE, при этом поле note заполняем пустой строкой.
        updatedSession = new CheckinSession(session.userId(), CheckinStep.PREVIEW, session.score(), "");

        // Сохраняем новую сессию в хранилище (она заменит старую по контракту save)
        // При этом session почистит GC
        sessionStore.save(updatedSession);

        return buildPreview(updatedSession);

    }

    /**
     * Строит текст карточки предпросмотра.
     *
     * @param session текущая сессия
     * @return карточка с оценкой, заметкой и командами подтверждения
     */
    private String buildPreview(CheckinSession session) {
        String note = session.note();
        return "📋 Предпросмотр карточки:\n" + "• Оценка: " + session.score() + "/" + MAX_SCORE + "\n" + "• Заметка:" + (note.isEmpty() ? "" : " " + note) + "\n" + "Подтвердите запись:\n" + "/approve — сохранить\n" + "/reject — отменить";
    }

    /**
     * Проверяет наличие открытой сессии у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return true, если у пользователя есть открытый диалог
     */
    public boolean hasActiveSession(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        // Делегирование в хранилище сессий
        return sessionStore.hasSession(userId);
    }
}
