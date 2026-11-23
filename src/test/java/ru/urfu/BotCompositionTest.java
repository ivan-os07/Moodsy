package ru.urfu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.FieldSource;
import ru.urfu.commands.CommandDispatcher;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

class BotCompositionTest {
    /**
     * Сообщение о неизвестной команде.
     */
    private static final String UNKNOWN = "Неизвестная команда";

    /**
     * Зарегистрированные команды
     */
    private final static List<String> COMMANDS = List.of(
            "/start",
            "/help",
            "/checkin",
            "/history",
            "/stats"
    );


    /**
     * Композитор
     */
    private final BotComposition composition = new BotComposition(
            Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"),
                    ZoneOffset.UTC
            ));

    /**
     * Проверка, что все команды подключены
     *
     * @param key команда
     */
    @ParameterizedTest
    @FieldSource("COMMANDS")
    void commandIsConnected(String key) {
        String reply = composition.getDispatcher().handle(UUID.randomUUID(), key).text();

        Assertions.assertFalse(
                reply.contains(UNKNOWN),
                "Команда не подключена: " + key
        );
    }


    /**
     * Проверка, что вывод команды /help содержит зарегистрированные команды
     */
    @Test
    void helpListsAllCommands() {
        String reply = composition.getDispatcher().handle(UUID.randomUUID(), "/help").text();

        for (String key : COMMANDS) {
            Assertions.assertTrue(reply.contains(key), "В /help нет " + key);
        }
    }

    /**
     * CheckinCommand и диспетчер используют один сервис чекина
     */
    @Test
    void checkinCommandAndDispatcherShareFsm() {
        UUID user = UUID.randomUUID();
        composition.getDispatcher().handle(user, "/checkin");

        String reply = composition.getDispatcher().handle(user, "7").text();

        Assertions.assertFalse(reply.contains(UNKNOWN), "Диспетчер не видит сессию чекина");
    }


    /**
     * Чекин попадает в историю
     */
    @Test
    void checkinShowsUpInHistory() {
        UUID user = UUID.randomUUID();
        CommandDispatcher dispatcher = composition.getDispatcher();

        Assertions.assertTrue(dispatcher
                .handle(user, "/history")
                .text()
                .contains("История пуста")
        );

        dispatcher.handle(user, "/checkin");
        dispatcher.handle(user, "7");
        dispatcher.handle(user, "Дела");
        dispatcher.handle(user, "много тикетов");
        dispatcher.handle(user, "/approve");

        String history = dispatcher.handle(user, "/history").text();
        Assertions.assertTrue(history.contains("7/10"));
    }
}