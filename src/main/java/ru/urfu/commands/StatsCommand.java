package ru.urfu.commands;

import ru.urfu.model.MoodEntry;
import ru.urfu.repository.MoodRepository;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.analytics.MoodStatistics;
import ru.urfu.service.analytics.StatisticsPeriod;

import java.text.DecimalFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class StatsCommand implements BotCommand {
    private final MoodRepository moodRepository;
    private final MoodAnalyticsService analyticsService;
    private final Clock clock;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    private final String usageMessage = """
            ❌ Неверные аргументы.
            Использование: /stats <период>
            Доступные периоды: week, month.
            """;

    public StatsCommand(MoodRepository moodRepository, MoodAnalyticsService analyticsService, Clock clock) {
        this.moodRepository = Objects.requireNonNull(
                moodRepository, "moodRepository не может быть null");
        this.analyticsService = Objects.requireNonNull(
                analyticsService, "analyticsService не должен быть null");
        this.clock = Objects.requireNonNull(
                        clock, "clock не должен быть null")
                .withZone(ZoneOffset.ofHours(3));


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
            return usageMessage;
        }

        StatisticsPeriod period = findPeriod(args[0]);
        if (period == null) {
            return usageMessage;
        }
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime from = period.getStart(now);
        LocalDateTime toExclusive = period.getEndExclusive(now);

        List<MoodEntry> entries = moodRepository.findByPeriod(userId, from, toExclusive);

        if (entries.isEmpty()) {
            return "Записей нет, используйте /checkin";
        }

        MoodStatistics statistics = analyticsService.calculate(entries);
        return formatStatistics(period, from, toExclusive, statistics);
    }


    private StatisticsPeriod findPeriod(String argument) {
        for (StatisticsPeriod period : StatisticsPeriod.values()) {
            if (period.getArgument().equalsIgnoreCase(argument)) {
                return period;
            }
        }
        return null;
    }

    private String formatStatistics(
            StatisticsPeriod period,
            LocalDateTime from,
            LocalDateTime toExclusive,
            MoodStatistics statistics) {
        DecimalFormat averageFormatter = new DecimalFormat("0.00");
        StringBuilder result = new StringBuilder();

        result.append("\uD83D\uDCCA Статистика за ")
                .append(period.getDisplayName())
                .append(" (")
                .append(from.format(dateFormatter))
                .append(" - ")
                .append(toExclusive.minusDays(1).format(dateFormatter))
                .append(")\n");

        result.append("Средняя оценка; ")
                .append(averageFormatter.format(statistics.averageScore()))
                .append("/10\n");

        result.append("Лучший день: ")
                .append(formatEntry(statistics.bestDay()))
                .append("\n");
        result.append("Худщий день: ")
                .append(formatEntry(statistics.worstDay()))
                .append("\n");
        result.append("Самая частая сфера: ")
                .append(statistics.mostFrequentSphere().getDisplayName())
                .append(" (")
                .append(statistics.sphereCount())
                .append(" ")
                .append(getMarkWord(statistics.sphereCount()))
                .append(")");
        return result.toString();
    }
    private String formatEntry(MoodEntry entry ) {
        return entry.createdAt().format(dateTimeFormatter)
                + " - " + entry.score() + "/10";
    }
    private String getMarkWord(int count) {
        int lastTwoDigits = count % 100;
        if (lastTwoDigits >= 11 && lastTwoDigits <= 14) {
            return "отметок";
        }
        int lastDigit = count % 10;
        if (lastDigit == 1) return "отметка";
        if (lastDigit >= 2 && lastDigit <= 4) {
            return "отметки";
        }
        return "отметок";


    }
}