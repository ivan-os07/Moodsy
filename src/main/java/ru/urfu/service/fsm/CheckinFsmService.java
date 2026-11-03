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
 * Управляет пошаговым диалогом чекина: проверяет ввод, переводит пользователя
 * между шагами и сохраняет готовую запись в репозиторий.
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
     * Сообщение о невозможности вернуться назад с первого шага.
     */
    private static final String FIRST_STEP = "❌ Вы на первом шаге, вернуться назад нельзя.";

    /**
     * Подсказка о доступных действиях на шаге предпросмотра.
     */
    private static final String PREVIEW_HINT =
            "❌ Пожалуйста, подтвердите запись с помощью /approve или отмените через /reject.";

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
        this.clock = Objects.requireNonNull(clock, "clock не может быть null")
                .withZone(ZoneOffset.ofHours(3));
    }

    /**
     * Создаёт сессию на шаге SCORE и просит ввести оценку.
     *
     * @param userId идентификатор пользователя
     * @return приглашение ввести оценку
     */
    public String start(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        CheckinSession session = new CheckinSession(userId);
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
        Optional<CheckinSession> found = sessionStore.findById(userId);
        if (found.isEmpty()) {
            return NO_SESSION;
        }
        CheckinSession session = found.get();
        String input = text == null ? "" : text.trim();
        String command = input.toLowerCase(Locale.ROOT);

        if (CMD_EXIT.equals(command)) {
            return handleExit(session);
        }
        if (CMD_BACK.equals(command)) {
            return back(session);
        }

        return switch (session.getStep()) {
            case SCORE -> handleScore(session, input);
            case NOTE -> handleNote(session, input);
            case PREVIEW -> handlePreview(session, command);
            default -> throw new IllegalStateException("Неизвестный шаг: " + session.getStep());
        };
    }

    /**
     * Проверяет наличие открытой сессии у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return true, если у пользователя есть открытый диалог
     */
    public boolean hasActiveSession(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        return sessionStore.hasSession(userId);
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
     * Сохраняет запись из предпросмотра и удаляет сессию.
     *
     * @param session текущая сессия
     * @return сообщение о сохранении записи
     */
    private String handleApprove(CheckinSession session) {
        MoodEntry entry = new MoodEntry(
                LocalDateTime.now(clock),
                session.getScore(),
                session.getNote());

        moodRepository.save(session.getUserId(), entry);
        sessionStore.remove(session.getUserId());
        return SAVED;
    }

    /**
     * Удаляет сессию без сохранения записи.
     *
     * @param session текущая сессия
     * @return сообщение о завершении диалога
     */
    private String handleExit(CheckinSession session) {
        sessionStore.remove(session.getUserId());
        return EXITED;
    }

    /**
     * Проверяет оценку и переводит диалог на шаг NOTE.
     *
     * @param session текущая сессия
     * @param text    введённый текст
     * @return приглашение ввести заметку или сообщение об ошибке
     */
    private String handleScore(CheckinSession session, String text) {
        int score;
        try {
            score = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            return SCORE_ERROR;
        }
        if (score < MIN_SCORE || score > MAX_SCORE) {
            return SCORE_ERROR;
        }
        session.applyScore(score);
        return NOTE_PROMPT;
    }

    /**
     * Принимает заметку или пропускает её и переводит диалог на шаг PREVIEW.
     *
     * @param session текущая сессия
     * @param text    введённый текст
     * @return текст карточки предпросмотра или сообщение об ошибке
     */
    private String handleNote(CheckinSession session, String text) {
        if (CMD_SKIP.equalsIgnoreCase(text)) {
            session.skipNote();
        } else if (text.startsWith("/")) {
            return UNKNOWN_COMMAND;
        } else {
            session.applyNote(text);
        }
        return buildPreview(session);
    }

    /**
     * Переводит диалог на предыдущий шаг и сообщает, куда он вернулся.
     *
     * @param session текущая сессия
     * @return приглашение предыдущего шага или сообщение о невозможности возврата
     */
    private String back(CheckinSession session) {
        if (!session.goBack()) {
            return FIRST_STEP;
        }
        if (session.getStep() == CheckinStep.SCORE) {
            return SCORE_PROMPT;
        }
        return NOTE_PROMPT;
    }

    /**
     * Строит текст карточки предпросмотра.
     *
     * @param session текущая сессия
     * @return карточка с оценкой, заметкой и командами подтверждения
     */
    private String buildPreview(CheckinSession session) {
        String note = session.getNote();
        return "📋 Предпросмотр карточки:\n"
                + "• Оценка: " + session.getScore() + "/" + MAX_SCORE + "\n"
                + "• Заметка:" + (note.isEmpty() ? "" : " " + note) + "\n"
                + "Подтвердите запись:\n"
                + "/approve — сохранить\n"
                + "/reject — отменить";
    }
}
