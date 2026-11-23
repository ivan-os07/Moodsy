package ru.urfu;

import ru.urfu.commands.*;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.repository.InMemoryUserRepository;
import ru.urfu.repository.MoodRepository;
import ru.urfu.repository.UserRepository;
import ru.urfu.service.UserService;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.fsm.checkin.CheckinFsmService;
import ru.urfu.service.fsm.checkin.CheckinSessionStore;
import ru.urfu.service.fsm.checkin.InMemoryCheckinSessionStore;
import ru.urfu.service.fsm.stats.StatsFsmService;

import java.time.Clock;
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
    private final MoodAnalyticsService analyticsService;

    /**
     * Собирает все компоненты приложения.
     *
     * @param clock часы для сервиса чекина
     */
    public BotComposition(Clock clock) {
        Objects.requireNonNull(clock, "clock не может быть null");

        // Данные
        MoodRepository moodRepository = new InMemoryMoodRepository();
        UserRepository userRepository = new InMemoryUserRepository();
        this.userService = new UserService(userRepository);
        this.analyticsService = new MoodAnalyticsService();

        // Диалог чекина: один экземпляр на команду и диспетчер
        CheckinSessionStore sessionStore = new InMemoryCheckinSessionStore();
        CheckinFsmService checkinFsmService = new CheckinFsmService(sessionStore, moodRepository, clock);
        StatsFsmService statsFsmService = new StatsFsmService(moodRepository, analyticsService, clock);
        // Команды
        this.registry = new CommandRegistry();
        registry.register(new StartCommand());
        registry.register(new HelpCommand(registry));
        registry.register(new CheckinCommand(checkinFsmService));
        registry.register(new StatsCommand(statsFsmService));
        registry.register(new HistoryCommand(moodRepository));

        this.dispatcher = new CommandDispatcher(registry, checkinFsmService, statsFsmService );
    }

    /**
     * Для получения регистратора команд.
     * Нужен для тестов.
     * @return регистратор команд
     */
    CommandRegistry getRegistry() {
        return registry;
    }

    /**
     * Для получения диспетчера
     * @return диспетчер
     */
    public CommandDispatcher getDispatcher() {
        return dispatcher;
    }

    /**
     * Для получения сервиса пользователей
     * @return сервиса пользователей
     */
    public UserService getUserService() {
        return userService;
    }
}