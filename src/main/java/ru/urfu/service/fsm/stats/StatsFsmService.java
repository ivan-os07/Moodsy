package ru.urfu.service.fsm.stats;

import ru.urfu.application.Keyboard;
import ru.urfu.application.KeyboardButton;
import ru.urfu.application.KeyboardRow;
import ru.urfu.model.MoodEntry;
import ru.urfu.repository.MoodRepository;
import ru.urfu.service.analytics.MoodAnalyticsService;
import ru.urfu.service.analytics.MoodStatistics;
import ru.urfu.service.analytics.StatisticsPeriod;
import ru.urfu.service.fsm.FsmResult;
import ru.urfu.service.fsm.FsmService;

import java.text.DecimalFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class StatsFsmService implements FsmService {
    private static final String PERIOD_MESSAGE = "Выберите период статистики:";
    private static final String PERIOD_ERROR = "❌ Ошибка: необходимо выбрать один из предложенных периодов";


    private static final String NO_ENTRIES = "Записей нет, используйте /checkin";
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private final Set<UUID> activeSessions = new HashSet<>();
    private final MoodRepository moodRepository;
    private final MoodAnalyticsService moodAnalyticsService;
    private final Clock clock;

    public StatsFsmService(
            MoodRepository moodRepository,
            MoodAnalyticsService analyticsService,
            Clock clock
    ) {
        this.moodRepository = Objects.requireNonNull(moodRepository);
        this.moodAnalyticsService = Objects.requireNonNull(analyticsService);
        this.clock = Objects.requireNonNull(clock).withZone(ZoneOffset.ofHours(3));
    }

    public String start(UUID userId) {
        Objects.requireNonNull(userId);
        activeSessions.add(userId);
        return PERIOD_MESSAGE;
    }

    public FsmResult startWithResponse(UUID userId) {
        String text = start(userId);
        return new FsmResult(text, Optional.of(createKeyboard()));
    }

    @Override
    public FsmResult handleWithResponse(UUID userId, String text) {
        Objects.requireNonNull(userId);
        if (!activeSessions.contains(userId)) {
            return new FsmResult(text, Optional.empty());
        }
        String input = text == null ? "" : text.trim();
        StatisticsPeriod period = findPeriod(input);
        if (period == null) {
            return new FsmResult(PERIOD_ERROR, Optional.of(createKeyboard()));
        }
        activeSessions.remove(userId);
        String response = calculateStatistics(userId, period);
        return new FsmResult(response, Optional.empty());
    }


    @Override
    public boolean hasActiveSession(UUID userId) {
        Objects.requireNonNull(userId);
        return activeSessions.contains(userId);
    }

    private String calculateStatistics(UUID userId, StatisticsPeriod period){
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime from = period.getStart(now);
        LocalDateTime toExclusive = period.getEndExclusive(now);
        List<MoodEntry> entries = moodRepository.findByPeriod(userId, from, toExclusive);
        if (entries.isEmpty()) {
            return NO_ENTRIES;
        }
        MoodStatistics statistics = moodAnalyticsService.calculate(entries);
        return formatStatistics(period, from, toExclusive, statistics);
    }
    /**
     * Находит период по аргументу команды
     *
     * @param argument аргумент периода
     * @return найденный период или null, если неизвестный аргумент
     */
    private StatisticsPeriod findPeriod(String argument) {
        for (StatisticsPeriod period : StatisticsPeriod.values()) {
            if (period.getArgument().equalsIgnoreCase(argument)) {
                return period;
            }
        }
        return null;
    }

    /**
     * Формирует итоговое сообщение пользователю
     *
     * @param period      выбранный календарный период
     * @param from        начало периода включительно
     * @param toExclusive конец периода исключительно
     * @param statistics  рассчитанные показатели
     * @return текст статистики для пользователя
     */
    private String formatStatistics(
            StatisticsPeriod period,
            LocalDateTime from,
            LocalDateTime toExclusive,
            MoodStatistics statistics) {
        DecimalFormat averageFormatter = new DecimalFormat("0.00");

        return "\uD83D\uDCCA Статистика за " +
                period.getDisplayName() +
                " (" +
                from.format(dateFormatter) +
                " - " +
                toExclusive.minusDays(1).format(dateFormatter) +
                ")\n" +
                "Средняя оценка; " +
                averageFormatter.format(statistics.averageScore()) +
                "/10\n" +
                "Лучший день: " +
                formatEntry(statistics.bestDay()) +
                "\n" +
                "Худший день: " +
                formatEntry(statistics.worstDay()) +
                "\n" +
                "Самая частая сфера: " +
                statistics.mostFrequentSphere().getDisplayName() +
                " (" +
                statistics.sphereCount() +
                " " +
                getMarkWord(statistics.sphereCount()) +
                ")";
    }

    /**
     * Формирует дату, время и оценку одной отметки
     *
     * @param entry отметка пользователя
     * @return отформатированный текст
     */
    private String formatEntry(MoodEntry entry) {
        return entry.createdAt().format(dateTimeFormatter)
                + " - " + entry.score() + "/10";
    }

    /**
     * Возвращает правильную форму слова "отметка" для заданного количества
     *
     * @param count количество отметок
     * @return правильная форма слова "отметка"
     */
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

    private Keyboard createKeyboard() {
        List<KeyboardRow> rows = new ArrayList<>();
        rows.add(new KeyboardRow(List.of(
                new KeyboardButton("Неделя", "week"),
                new KeyboardButton("Месяц", "month")
        )));
        return new Keyboard(rows);
    }
}
