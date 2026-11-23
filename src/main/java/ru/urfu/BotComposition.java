package ru.urfu;

import ru.urfu.commands.CheckinCommand;
import ru.urfu.commands.CommandDispatcher;
import ru.urfu.commands.CommandRegistry;
import ru.urfu.commands.HelpCommand;
import ru.urfu.commands.HistoryCommand;
import ru.urfu.commands.StartCommand;
import ru.urfu.repository.InMemoryMoodRepository;
import ru.urfu.repository.InMemoryUserRepository;
import ru.urfu.repository.MoodRepository;
import ru.urfu.repository.UserRepository;
import ru.urfu.service.UserService;
import ru.urfu.service.fsm.CheckinFsmService;
import ru.urfu.service.fsm.CheckinSessionStore;
import ru.urfu.service.fsm.InMemoryCheckinSessionStore;

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

        // Диалог чекина: один экземпляр на команду и диспетчер
        CheckinSessionStore sessionStore = new InMemoryCheckinSessionStore();
        CheckinFsmService checkinFsmService = new CheckinFsmService(sessionStore, moodRepository, clock);

        // Команды
        this.registry = new CommandRegistry();
        registry.register(new StartCommand());
        registry.register(new HelpCommand(registry));
        registry.register(new CheckinCommand(checkinFsmService));
        registry.register(new HistoryCommand(moodRepository));

        this.dispatcher = new CommandDispatcher(registry, checkinFsmService);
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