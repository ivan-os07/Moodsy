package ru.urfu.service.fsm.checkin;

import ru.urfu.application.BotResponse;
import ru.urfu.application.Keyboard;
import ru.urfu.application.KeyboardButton;
import ru.urfu.application.KeyboardRow;
import ru.urfu.model.MoodEntry;
import ru.urfu.model.Sphere;
import ru.urfu.repository.MoodRepository;
import ru.urfu.service.fsm.FsmService;
import ru.urfu.service.fsm.StepTransition;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

/**
 * Управляет пошаговым диалогом чекина (управляет сессией):
 * <ul>
 *     <li>Проверяет ввод</li>
 *     <li>Переводит пользователя между шагами</li>
 *     <li>Сохраняет готовую запись в репозиторий</li>
 * </ul>
 */
public class CheckinFsmService implements FsmService<CheckinSession> {

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
     * Сообщение о выборе сферы
     */
    private static final String SPHERE_PROMPT = """
            Выберете сферу из предложенных:
            Дела, Общение, Здоровье, Отдых
            """;

    /**
     * Сообщение об ошибке оценки.
     */
    private static final String SCORE_ERROR = "❌ Ошибка: оценка настроения должна быть числом от 1 до 10.";

    /**
     * Сообщение о неизвестной команде на шаге заметки.
     */
    private static final String UNKNOWN_COMMAND = "❌ Неизвестная команда. Доступны: /skip, /back, /exit";

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
     * Сообщение об успешном сохранении записи.
     */
    private static final String SAVED = "✅ Запись сохранена.";

    /**
     * Сообщение о завершении диалога без сохранения.
     */
    private static final String EXITED = "Сессия завершена без сохранения отметки";

    /**
     * Сообщение об ошибке при выборе сферы
     */
    private static final String SPHERE_ERROR = "❌Ошибка: НЕОБХОДИМО выбрать только из предложенных сфер.";

    /**
     * Репозиторий записей настроения.
     */
    private final MoodRepository moodRepository;

    /**
     * Источник времени, приведённый к московскому (UTC+3).
     */
    private final Clock clock;

    /**
     * Фабрика клавиатур для шагов чекина.
     */
    private final CheckinKeyboardFactory keyboardFactory;

    /**
     * Создаёт сервис диалога чекина.
     *
     * @param moodRepository  репозиторий записей настроения
     * @param clock           источник времени; время приводится к московскому (UTC+3)
     * @param keyboardFactory фабрика клавиатур для шагов чекина
     */
    public CheckinFsmService(
            MoodRepository moodRepository,
            Clock clock,
            CheckinKeyboardFactory keyboardFactory
    ) {
        this.moodRepository = Objects.requireNonNull(moodRepository, "moodRepository не может быть null");
        this.clock = Objects.requireNonNull(clock, "clock не может быть null").withZone(ZoneOffset.ofHours(3));
        this.keyboardFactory = Objects.requireNonNull(keyboardFactory, "keyboardFactory не может быть null");
    }

    @Override
    public StepTransition start(UUID userId) {
        return continueStep(SCORE_PROMPT, new CheckinSession(userId));
    }

    @Override
    public StepTransition handle(CheckinSession session, String text) {
        Objects.requireNonNull(session, "session не может быть null");

        String input = text == null ? "" : text.trim();
        String command = input.toLowerCase(Locale.ROOT);

        return switch (command) {
            case CMD_BACK -> handleBack(session);
            case CMD_SKIP -> handleSkip(session);
            default -> switch (session.step()) {
                case SCORE -> handleScore(session, input);
                case SPHERE -> handleSphere(session, input);
                case NOTE -> handleNote(session, input);
                case PREVIEW -> handlePreview(session, command);
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
    private StepTransition handlePreview(CheckinSession session, String command) {
        if (CMD_APPROVE.equals(command)) {
            return handleApprove(session);
        }
        if (CMD_REJECT.equals(command)) {
            return finishStep(EXITED);
        }
        return continueStep(PREVIEW_HINT, session);
    }

    /**
     * Принимает оценку на шаге SCORE и переводит диалог на шаг SPHERE.
     *
     * @param session текущая сессия
     * @param text    введённый текст
     * @return приглашение ввести сферу или сообщение об ошибке
     */
    private StepTransition handleScore(CheckinSession session, String text) {
        // тк мы приняли сессию, то она точно не пуста (мы проверили ее в handle)
        int score;
        try {
            score = Integer.parseInt(text);
        } catch (NumberFormatException e) {
            // Сессия остается та же самая(с теми же данными)
            return continueStep(SCORE_ERROR, session);
        }

        // валидация
        if (score < MIN_SCORE || score > MAX_SCORE) {
            // Сессия остается та же самая(с теми же данными)
            return continueStep(SCORE_ERROR, session);
        }

        return continueStep(
                SPHERE_PROMPT,
                session.withScore(score, CheckinStep.SPHERE)
        );
    }

    /**
     * Принимает название сферы на шаге SPHERE и переводит диалог на шаг NOTE.
     *
     * @param session текущая сессия
     * @param text    введённый текст
     * @return приглашение ввести заметку или сообщение об ошибке
     */
    private StepTransition handleSphere(CheckinSession session, String text) {
        // тк мы приняли сессию, то она точно не пуста (мы проверили ее в handle)
        if (text.startsWith("/")) {
            return continueStep(UNKNOWN_COMMAND, session);
        }

        // валидация сферы
        Optional<Sphere> resolved = resolveSphere(text);
        return resolved.map(sphere -> continueStep(
                NOTE_PROMPT,
                session.withSphere(sphere, CheckinStep.NOTE)
        )).orElseGet(() -> continueStep(SPHERE_ERROR, session));

    }


    /**
     * Принимает заметку или пропускает(/skip) её и переводит диалог на шаг PREVIEW.
     *
     * @param session текущая сессия
     * @param text    введённый текст
     * @return текст карточки предпросмотра или сообщение об ошибке
     */
    private StepTransition handleNote(CheckinSession session, String text) {
        if (text.startsWith("/")) {
            return continueStep(UNKNOWN_COMMAND, session);
        }

        CheckinSession updatedSession = session.withNote(text, CheckinStep.PREVIEW);
        return continueStep(buildPreview(updatedSession), updatedSession);
    }


    /**
     * Сохраняет запись из предпросмотра и удаляет сессию.
     *
     * @param session текущая сессия
     * @return сообщение о сохранении записи
     */
    private StepTransition handleApprove(CheckinSession session) {
        MoodEntry entry = new MoodEntry(
                LocalDateTime.now(clock),
                session.score(),
                session.sphere(),
                session.note()
        );
        moodRepository.save(session.userId(), entry);
        return finishStep(SAVED);
    }


    /**
     * Переводит диалог на предыдущий шаг.
     */
    private StepTransition handleBack(CheckinSession session) {
        return switch (session.step()) {
            case SCORE -> continueStep(FIRST_STEP, session);
            case SPHERE -> continueStep(SCORE_PROMPT, session.withStep(CheckinStep.SCORE));
            case NOTE -> continueStep(SPHERE_PROMPT, session.withStep(CheckinStep.SPHERE));
            case PREVIEW -> continueStep(NOTE_PROMPT, session.withStep(CheckinStep.NOTE));
        };
    }

    /**
     * Обрабатывает команду /skip (доступна только на шаге NOTE).
     */
    private StepTransition handleSkip(CheckinSession session) {
        if (session.step() != CheckinStep.NOTE) {
            String message = session.step() == CheckinStep.PREVIEW
                    ? SKIP_ERROR + '\n' + PREVIEW_HINT
                    : SKIP_ERROR;
            return continueStep(message, session);
        }

        CheckinSession updatedSession = session.withNote("", CheckinStep.PREVIEW);
        return continueStep(buildPreview(updatedSession), updatedSession);
    }

    /**
     * Строит текст карточки предпросмотра.
     *
     * @param session текущая сессия
     * @return карточка с оценкой, сферой, заметкой и командами подтверждения
     */
    private String buildPreview(CheckinSession session) {
        String note = session.note();
        return """
                📋 Предпросмотр карточки:
                • Оценка: %s/%s
                • Сфера: %s
                • Заметка:%s
                Подтвердите запись:
                /approve — сохранить
                /reject — отменить
                """.formatted(session.score(),
                MAX_SCORE,
                session.sphere().getDisplayName(),
                note.isEmpty() ? "" : " " + note
        );

    }


    /**
     * Валидация сферы
     *
     * @param input потенциальная сфера
     * @return сфера если прошла валидация, иначе пустой Optional
     */
    private Optional<Sphere> resolveSphere(String input) {
        for (Sphere sphere : Sphere.values()) {
            if (sphere.getDisplayName().equalsIgnoreCase(input)) {
                return Optional.of(sphere);
            }
        }
        return Optional.empty();
    }

    /**
     * Формирует переход с сохранением сессии и клавиатурой для её шага.
     */
    private StepTransition continueStep(String text, CheckinSession nextSession) {
        BotResponse response = new BotResponse(text, keyboardFactory.create(nextSession.step()));
        return new StepTransition(response, nextSession);
    }

    /**
     * Формирует завершающий переход без клавиатуры и без следующей сессии.
     */
    private StepTransition finishStep(String text) {
        return new StepTransition(new BotResponse(text));
    }
}
