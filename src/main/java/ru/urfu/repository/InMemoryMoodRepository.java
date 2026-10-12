package ru.urfu.repository;

import ru.urfu.service.MoodEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Реализация репозитория для хранения записей в оперативной памяти
 */
public class InMemoryMoodRepository implements MoodRepository {

    /**
     * Записи настроения, разделенные по идентификаторам пользователей
     */
    private final ConcurrentHashMap<String, List<MoodEntry>> entries = new ConcurrentHashMap<>();

    /**
     * Сохраняет новую запись (отметку) пользователя
     *
     * @param userId идентификатор пользователя
     * @param entry  запись (отметка)
     */

    @Override
    public void save(String userId, MoodEntry entry) {
        entries.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(entry);

    }

    /**
     * Возвращает последние записи пользователя (от новых к старым)
     *
     * @param userId идентификатор пользователя
     * @param limit  максимальное число возвращаемых записей
     * @return список последних записей
     */
    @Override
    public List<MoodEntry> findLast(String userId, int limit) {
        if (limit <= 0) {
            return Collections.emptyList();
        }
        List<MoodEntry> userEntries = entries.get(userId);
        if (userEntries == null || userEntries.isEmpty()) {
            return Collections.emptyList();

        }
        int end = userEntries.size();
        int start = Math.max(0, end - limit);
        List<MoodEntry> result = new ArrayList<>(userEntries.subList(start, end));
        Collections.reverse(result);
        return Collections.unmodifiableList(result);
    }

    /**
     * Возвращает последнюю запись пользователя
     *
     * @param userId идентификатор пользователя
     * @return последняя запись, иначе пустой Optional
     */
    @Override
    public Optional<MoodEntry> findLatest(String userId) {
        List<MoodEntry> userEntries = entries.get(userId);
        if (userEntries == null || userEntries.isEmpty()) {
            return Optional.empty();

        }
        return Optional.of(userEntries.get(userEntries.size() - 1));
    }

    /**
     * Обновляет последнюю запись пользователя
     *
     * @param userId       идентификатор пользователя
     * @param updatedEntry новая версия записи
     * @return true, если обновлена, иначе false
     */
    @Override
    public boolean updateLast(String userId, MoodEntry updatedEntry) {
        List<MoodEntry> userEntries = entries.get(userId);
        if (userEntries == null || userEntries.isEmpty()) {
            return false;
        }
        userEntries.set(userEntries.size() - 1, updatedEntry);
        return true;
    }

    /**
     * Удаляет последнюю запись пользователя
     * @param userId идентификатор пользователя
     * @return true, если запись удалена, иначе false
     */
    @Override
    public boolean deleteLast(String userId) {
        List<MoodEntry> userEntries = entries.get(userId);
        if (userEntries == null || userEntries.isEmpty()) {
            return false;

        }
        userEntries.removeLast();
        return true;
    }
}

