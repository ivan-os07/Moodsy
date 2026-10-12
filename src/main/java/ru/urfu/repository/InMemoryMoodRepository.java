package ru.urfu.repository;

import ru.urfu.service.MoodEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


public class InMemoryMoodRepository implements MoodRepository {
    private final ConcurrentHashMap<String, List<MoodEntry>> entries = new ConcurrentHashMap<>();


    @Override
    public void save(String userId, MoodEntry entry) {
        entries.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(entry);

    }
    @Override
    public List<MoodEntry> findLast(String userId, int limit){
        if (limit <=0){
            return Collections.emptyList();
        }
        List<MoodEntry> userEntries = entries.get(userId);
        if (userEntries == null || userEntries.isEmpty()){
            return Collections.emptyList();

        }
        int end = userEntries.size();
        int start = Math.max(0, end - limit);
        List<MoodEntry> result = new ArrayList<>(userEntries.subList(start,end));
        Collections.reverse(result);
        return Collections.unmodifiableList(result);
    }
}
