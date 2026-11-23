package ru.urfu.repository;

import ru.urfu.model.MoodEntry;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Реализация репозитория для хранения записей в оперативной памяти
 */
public class InMemoryMoodRepository implements MoodRepository {

    /**
     * Записи настроения, разделенные по внутренним идентификаторам пользователей
     */
    private final Map<UUID, List<MoodEntry>> entries = new ConcurrentHashMap<>();


    @Override
    public void save(UUID userId, MoodEntry entry) {
        entries.computeIfAbsent(userId, k -> new ArrayList<>())
                .add(entry);

    }


    @Override
    public List<MoodEntry> findLast(UUID userId, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        List<MoodEntry> userEntries = entries.get(userId);
        if (userEntries == null || userEntries.isEmpty()) {
            return Collections.emptyList();

        }
        List<MoodEntry> result;

        int end = userEntries.size();
        int start = Math.max(0, end - limit);
        result = new ArrayList<>(end - start);
        // Заполняем с конца
        for (int i = end - 1; i >= start; i--) {
            result.add(userEntries.get(i));

        }
        return Collections.unmodifiableList(result);
    }
    @Override
    public List<MoodEntry> findByPeriod(UUID userId, LocalDateTime from, LocalDateTime to){
        Objects.requireNonNull(userId, "userId не может быть null");
        Objects.requireNonNull(to, "to не может быть null");
        Objects.requireNonNull(from, "from не может быть null");

        if (!from.isBefore(to)) {
            throw new IllegalArgumentException("Начало периода должно быть раньше конца");
        }

        List<MoodEntry> userEntries = entries.get(userId);

        if (userEntries == null || userEntries.isEmpty()) {
            return Collections.emptyList();
        }

        return userEntries.stream()
                .filter(entry ->
                        !entry.createdAt().isBefore(from)
                                && entry.createdAt().isBefore(to))
                .toList();



    }
}

