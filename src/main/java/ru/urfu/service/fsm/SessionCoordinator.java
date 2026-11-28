package ru.urfu.service.fsm;

import ru.urfu.application.BotResponse;
import ru.urfu.service.fsm.checkin.CheckinSession;
import ru.urfu.service.fsm.stats.StatsSession;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Координатор FSM.
 * Владеет хранилищем,
 * сам управляет стартом/перезаписью/глобальной отменой,
 * маршрутизирует ввод в нужный сценарий по типу сессии
 */
public class SessionCoordinator {

    private static final String CMD_EXIT = "/exit";
    private static final String NO_SESSION = "Активного диалога нет.";
    private static final String CANCELLED = "Диалог отменён.";

    /**
     * Хранилище открытых сессий диалогов.
     */
    private final SessionStore sessionStore;

    /**
     * Сервис чекина.
     */
    private final FsmService<CheckinSession> checkinFsm;

    /**
     * Сервис статистики.
     */
    private final FsmService<StatsSession> statsFsm;


    /**
     * Создаёт сервис координатора.
     *
     * @param sessionStore хранилище открытых сессий
     * @param checkinFsm   сервис чекина
     * @param statsFsm     сервис статистики
     * @throws NullPointerException если любой из аргументов равен {@code null}
     */
    public SessionCoordinator(SessionStore sessionStore, FsmService<CheckinSession> checkinFsm, FsmService<StatsSession> statsFsm) {
        this.sessionStore = Objects.requireNonNull(sessionStore, "sessionStore не может быть null");
        this.checkinFsm = Objects.requireNonNull(checkinFsm, "checkinFsm не может быть null");
        this.statsFsm = Objects.requireNonNull(statsFsm, "statsFsm не может быть null");
    }

    /**
     * Запускает диалог указанного типа.
     * Если у пользователя уже была открыта другая сессия, она перезаписывается.
     *
     * @param userId     идентификатор пользователя
     * @param dialogType тип запускаемого диалога
     * @return ответ первого шага диалога
     * @throws NullPointerException если {@code userId} или {@code dialogType} равны {@code null}
     */
    public BotResponse start(UUID userId, DialogType dialogType) {
        Objects.requireNonNull(userId, "userId не может быть null");
        Objects.requireNonNull(dialogType, "dialogType не может быть null");

        StepTransition transition = switch (dialogType) {
            case CHECKIN -> checkinFsm.start(userId);
            case STATS -> statsFsm.start(userId);
        };
        return applyTransition(userId, transition);
    }


    /**
     * Обрабатывает ввод пользователя в рамках его активной сессии.
     *
     * @param userId идентификатор пользователя
     * @param text   текст сообщения или значение нажатой кнопки
     * @return ответ на текущий шаг диалога
     * @throws NullPointerException  если {@code userId} равен {@code null}
     * @throws IllegalStateException если неподдерживаемый тип сессии
     */
    public BotResponse handle(UUID userId, String text) {
        Objects.requireNonNull(userId, "userId не может быть null");

        Optional<UserSession> current = sessionStore.findById(userId);
        if (current.isEmpty()) {
            return new BotResponse(NO_SESSION);
        }

        String normalized = text == null ? "" : text.trim();
        if (CMD_EXIT.equalsIgnoreCase(normalized)) {
            sessionStore.remove(userId);
            return new BotResponse(CANCELLED);
        }

        StepTransition transition = switch (current.get()) {
            case CheckinSession session -> checkinFsm.handle(session, normalized);
            case StatsSession session -> statsFsm.handle(session, normalized);

            default -> throw new IllegalStateException(
                    "Неподдерживаемый тип сессии: " + current.get().getClass().getName()
            );
        };
        return applyTransition(userId, transition);
    }

    /**
     * Проверяет наличие активной сессии у пользователя.
     *
     * @param userId идентификатор пользователя
     * @return {@code true}, если у пользователя открыт любой диалог
     * @throws NullPointerException если {@code userId} равен {@code null}
     */
    public boolean hasActiveSession(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        return sessionStore.hasSession(userId);
    }

    /**
     * Принудительно закрывает активную сессию пользователя, если она есть.
     *
     * @param userId идентификатор пользователя
     * @throws NullPointerException если {@code userId} равен {@code null}
     */
    public void cancel(UUID userId) {
        Objects.requireNonNull(userId, "userId не может быть null");
        sessionStore.remove(userId);
    }

    /**
     * Применяет результат перехода к хранилищу сессий и возвращает ответ пользователю.
     *
     * @param userId     идентификатор пользователя
     * @param transition результат обработки шага
     * @return ответ пользователю
     */
    private BotResponse applyTransition(UUID userId, StepTransition transition) {
        Objects.requireNonNull(transition, "transition не может быть null");
        if (transition.nextSession().isPresent()) {
            sessionStore.save(transition.nextSession().get());
        } else {
            sessionStore.remove(userId);
        }
        return transition.response();
    }
}
