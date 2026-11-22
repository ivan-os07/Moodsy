package ru.urfu.commands;

import ru.urfu.model.MoodEntry;
import ru.urfu.repository.MoodRepository;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.analytics.MoodStatistics;
import ru.urfu.service.analytics.StatisticsPeriod;
import ru.urfu.service.fsm.FsmResult;
import ru.urfu.service.fsm.stats.StatsFsmService;

import java.text.DecimalFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * Выполняет команду /stats и формирует статистику за календарный месяц или неделю
 */
public class StatsCommand implements BotCommand {
    private final StatsFsmService statsFsmService;
    /**
     * Сообщение о корректном использовании команды, при некорректном вызове
     */
    private static final String ARGS_ERROR = """
            ❌ Неверные аргументы.
            Использование: /stats <период>
            Доступные периоды: week, month.
            """;

    /**
     * Создает команду статистики с репозиторием, сервисом аналитики и часами
     *
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
        return "Статистика ваших отметок: /stats <week, month>.";
    }

    @Override
    public String execute(UUID userId, String[] args) {
        if (args == null || args.length != 1) {
            return ARGS_ERROR;
        }
        return statsFsmService.start(userId);
    }
    @Override
    public BotResponse executeWithResponse(UUID userId, String[] args){
        if (args != null && args.length > 0) {
            return new BotResponse(userId, ARGS_ERROR);
        }
        FsmResult result = statsFsmService.startWithResponse(userId);
        return new BotResponse(userId, result.text(), result.keyboard());
    }

}