package ru.urfu.repository;

import ru.urfu.model.MoodEntry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Реализация репозитория для хранения записей в оперативной памяти
 */
public class InMemoryMoodRepository implements MoodRepository {

    /**
     * Записи настроения, разделенные по идентификаторам пользователей
     */
    private final Map<String, List<MoodEntry>> entries = new ConcurrentHashMap<>();


    @Override
    public void save(String userId, MoodEntry entry) {
        entries.computeIfAbsent(userId, key -> Collections.synchronizedList(new ArrayList<>())).add(entry);

    }


    @Override
    public List<MoodEntry> findLast(String userId, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        List<MoodEntry> userEntries = entries.get(userId);
        if (userEntries == null || userEntries.isEmpty()) {
            return Collections.emptyList();

        }
        List<MoodEntry> result;
        synchronized (userEntries) {
            int end = userEntries.size();
            int start = Math.max(0, end - limit);
            result = new ArrayList<>(end - start);
            // Заполняем с конца
            for (int i = end - 1; i >= start; i--) {
                result.add(userEntries.get(i));
            }
        }
        return Collections.unmodifiableList(result);
    }

}

