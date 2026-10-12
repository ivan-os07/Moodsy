package ru.urfu.repository;

import ru.urfu.service.MoodEntry;
import java.util.List;

public interface MoodRepository {
    void save(String userId, MoodEntry entry);
    List<MoodEntry> findLast(String userId, int limit);
}
