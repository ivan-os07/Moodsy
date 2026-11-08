package ru.urfu.repository;

import ru.urfu.model.MoodEntry;

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



    @Override
    public void save(String userId, MoodEntry entry) {
        entries.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(entry);

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
        int end = userEntries.size();
        int start = Math.max(0, end - limit);
        List<MoodEntry> result = new ArrayList<>(end - start);
        //Заполняем с конца
        for (int i = end -1; i >= start; i--){
            result.add(userEntries.get(i));
        }
        return Collections.unmodifiableList(result);
    }

}

