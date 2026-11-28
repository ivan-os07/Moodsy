package ru.urfu;

import ru.urfu.commands.*;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.repository.InMemoryUserRepository;
import ru.urfu.repository.MoodRepository;
import ru.urfu.repository.UserRepository;
import ru.urfu.service.UserService;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.fsm.FsmService;
import ru.urfu.service.fsm.InMemorySessionStore;
import ru.urfu.service.fsm.SessionCoordinator;
import ru.urfu.service.fsm.SessionStore;
import ru.urfu.service.fsm.checkin.CheckinFsmService;
import ru.urfu.service.fsm.checkin.CheckinKeyboardFactory;
import ru.urfu.service.fsm.checkin.CheckinSession;
import ru.urfu.service.fsm.stats.StatsFsmService;
import ru.urfu.service.fsm.stats.StatsSession;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.Objects;

/**
 * Composition root: единственное место, где создаются и связываются
 * компоненты приложения. Не зависит от Telegram, поэтому его можно
 * создавать в тестах.
 */
public class BotComposition {
    /**
     * Хранилище команд.
     */
    private final CommandRegistry registry;

    /**
     * Диспетчер, который маршрутизирует сообщения по командам.
     */
    private final CommandDispatcher dispatcher;

    /**
     * Сервис пользователей.
     */
    private final UserService userService;

    /**
     * Собирает все компоненты приложения.
     *
     * @param clock источник времени
     * @throws NullPointerException если {@code clock} равен {@code null}
     */
    public BotComposition(Clock clock) {
        Clock moscowClock = Objects.requireNonNull(clock, "clock не может быть null")
                .withZone(ZoneOffset.ofHours(3));

        // Данные и сервисы
        MoodRepository moodRepository = new InMemoryMoodRepository();
        UserRepository userRepository = new InMemoryUserRepository();
        this.userService = new UserService(userRepository);
        MoodAnalyticsService analyticsService = new MoodAnalyticsService();

        // FSM: единое хранилище, сценарии и координатор
        SessionStore sessionStore = new InMemorySessionStore();
        CheckinKeyboardFactory checkinKeyboardFactory = new CheckinKeyboardFactory();

        FsmService<CheckinSession> checkinFsm = new CheckinFsmService(
                moodRepository,
                moscowClock,
                checkinKeyboardFactory
        );
        FsmService<StatsSession> statsFsm = new StatsFsmService(
                moodRepository,
                analyticsService,
                moscowClock
        );
        SessionCoordinator sessionCoordinator = new SessionCoordinator(
                sessionStore,
                checkinFsm,
                statsFsm
        );

        // Команды
        this.registry = new CommandRegistry();
        registry.register(new StartCommand());
        registry.register(new HelpCommand(registry));
        registry.register(new CheckinCommand(sessionCoordinator));
        registry.register(new StatsCommand(sessionCoordinator));
        registry.register(new HistoryCommand(moodRepository));

        // Диспетчер
        this.dispatcher = new CommandDispatcher(registry, sessionCoordinator);
    }

    /**
     * Возвращает реестр команд (используется в тестах).
     *
     * @return реестр команд
     */
    CommandRegistry getRegistry() {
        return registry;
    }

    /**
     * Возвращает диспетчер входящих сообщений.
     *
     * @return диспетчер
     */
    public CommandDispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Возвращает сервис пользователей.
     *
     * @return сервис пользователей
     */
    public UserService getUserService() {
        return userService;
    }
}