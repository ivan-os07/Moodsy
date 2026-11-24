package ru.urfu.commands;

import ru.urfu.service.fsm.FsmResult;
import ru.urfu.service.fsm.stats.StatsFsmService;

import java.util.Objects;
import java.util.UUID;

/**
 * Выполняет команду /stats и формирует статистику за календарный месяц или неделю
 */
public class StatsCommand implements BotCommand {
    /**
     * Сервис конечного автомата, отвечающий за команду /stats
     */
    private final StatsFsmService statsFsmService;
    /**
     * Сообщение о корректном использовании команды, при некорректном вызове
     */
    private static final String ARGS_ERROR = "❌ Команда /stats вызывается без параметров";

    /**
     * Создает команду получения статистики
     *
     * @param statsFsmService сервис конечного автомата статистики
     * @throws NullPointerException если statsFsmService равно null
     */
    public StatsCommand(StatsFsmService statsFsmService) {
        this.statsFsmService = Objects.requireNonNull(statsFsmService, "statsFsmService не должен быть null");


    }

    @Override
    public String getCommandKey() {
        return "/stats";
    }

    @Override
    public String getDescription() {
        return "Получить статистику своих отметок";
    }

    @Override
    public String execute(UUID userId, String[] args) {
        if (args != null && args.length > 0) {
            return ARGS_ERROR;
        }
        return statsFsmService.start(userId);
    }

    @Override
    public BotResponse executeWithResponse(UUID userId, String[] args) {
        if (args != null && args.length > 0) {
            return new BotResponse(userId, ARGS_ERROR);
        }
        FsmResult result = statsFsmService.startWithResponse(userId);
        return new BotResponse(userId, result.text(), result.keyboard());
    }

}