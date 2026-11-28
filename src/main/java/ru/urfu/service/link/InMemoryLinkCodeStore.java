package ru.urfu.service.link;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Реализация хранилища кода для подключения новой платформы
 */
public class InMemoryLinkCodeStore implements LinkCodeStore {
    private final Map<UUID, UUID> codeToUser = new ConcurrentHashMap<>();

    @Override
    public boolean putIfAbsent(UUID code, UUID ownerUserId) {
        return codeToUser.putIfAbsent(code, ownerUserId) == null;
    }

    @Override
    public Optional<UUID> find(UUID code) {
        return Optional.ofNullable(codeToUser.get(code));
    }

    @Override
    public boolean remove(UUID code) {
        return codeToUser.remove(code) != null;
    }
}
