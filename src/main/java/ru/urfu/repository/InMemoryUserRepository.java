package ru.urfu.repository;

import ru.urfu.model.PlatformIdentity;
import ru.urfu.model.User;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryUserRepository implements UserRepository {

    private final ConcurrentHashMap<UUID, User> usersById = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<PlatformIdentity, UUID> userIdsByPlatform = new ConcurrentHashMap<>();


    @Override
    public Optional<User> findById(UUID id) {
        Objects.requireNonNull(id, "id не может быть null");
        return Optional.ofNullable(usersById.get(id));
    }

    @Override
    public Optional<User> findByPlatformIdentity(PlatformIdentity identity) {
        Objects.requireNonNull(identity, "identity не может быть null");
        UUID userId = userIdsByPlatform.get(identity);
        if (userId == null) {
            return Optional.empty();
        }
        return findById(userId);
    }

    //атомарность обеспечивается за счет помещения пользователя в usersById до сохранения связи
    @Override
    public User getOrCreate(PlatformIdentity identity) {
        Objects.requireNonNull(identity, "identity не может быть null");
        UUID userId = userIdsByPlatform.computeIfAbsent(identity, key -> {
            UUID newId = UUID.randomUUID();
            User newUser = new User(newId, List.of(key));
            usersById.put(newId, newUser);
            return newId;
        });
        User user = usersById.get(userId);
        if (user == null) {
            throw new IllegalStateException("Нарушена связь между пользователем и аккаунтом платформы");
        }
        return user;
    }

    @Override
    public User addPlatformIdentity(UUID userId, PlatformIdentity identity) {
        Objects.requireNonNull(identity, "identity не может быть null");
        Objects.requireNonNull(userId, "userId не может быть null");
        User user = usersById.get(userId);

        if (user == null) {
            throw new IllegalArgumentException("Пользователь не найден: " + userId);
        }
        UUID existingUserId = userIdsByPlatform.putIfAbsent(identity, userId);

        if (existingUserId != null && !existingUserId.equals(userId)) {
            throw new IllegalStateException("Аккаунт платформы уже привязан к другому пользователю");
        }

        try {
            user.addPlatformIdentity(identity);
            return user;
        } catch (RuntimeException e) {
            if (existingUserId == null) {
                userIdsByPlatform.remove(identity, userId);
            }
            throw e;
        }
    }
}
