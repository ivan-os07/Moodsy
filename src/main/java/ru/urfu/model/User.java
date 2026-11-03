package ru.urfu.model;

import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.List;

/**
 * Пользователь бота с внутренним идентификатором
 * и списком привязанных платформ
 */
public final class User {
    /**
     * Внутренний уникальный идентификатор пользователя
     */
    private final UUID id;
    /**
     * Список привязанных аккаунтов различных платформ
     */
    private final List<PlatformIdentity> platforms;

    /**
     * Создает пользователя с заданным UUID и списком платформ
     *
     * @param id        внутренний идентификатор пользователя
     * @param platforms список привязанных платформ
     */
    public User(UUID id, List<PlatformIdentity> platforms) {
        this.id = Objects.requireNonNull(id, "id не может быть null");
        Objects.requireNonNull(platforms, "platforms не может быть null");
        this.platforms = new ArrayList<>();

        for (PlatformIdentity identity : platforms) {
            addPlatformIdentity(identity);
        }

    }

    /**
     * Возвращает внутренний UUID пользователя
     * @return UUID пользователя
     */
    public UUID getId() {
        return id;
    }

    /**
     * Возвращает список привязанных платформ
     * @return неизменяемая копия списка
     */
    public List<PlatformIdentity> getPlatforms() {
        return List.copyOf(platforms);
    }

    /**
     * Добавляет пользователю новую платформенную идентичность,
     * если такая идентичность уже привязана - ничего не происходит
     *
     */
    public void addPlatformIdentity(PlatformIdentity identity) {
        Objects.requireNonNull(identity, "identity не может быть null");
        Objects.requireNonNull(identity.platform(), "platform не может быть null");

        for (PlatformIdentity current : platforms) {
            if (current.platform() == identity.platform()) {
                if (current.equals(identity)) {
                    return;
                }

                throw new IllegalArgumentException("У пользователя уже привязан аккаунт на платформе " + identity.platform());
            }
        }
        platforms.add(identity);
    }
}