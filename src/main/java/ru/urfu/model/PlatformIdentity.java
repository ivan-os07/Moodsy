package ru.urfu.model;


import java.util.Objects;

/**
 * Идентификатор пользователя на конкретной платформе
 *
 * @param platform       тип платформы
 * @param platformUserId идентификатор пользователя на конкретной платформе
 */
public record PlatformIdentity(PlatformType platform, String platformUserId) {
    /**
     * Проверяет тип платформы и непустой внешний идентификатор
     *
     * @throws NullPointerException     platform или platformUserId равны null
     * @throws IllegalArgumentException если внешний идентификатор пуст или состоит из пробелов
     */
    public PlatformIdentity {
        Objects.requireNonNull(platform, "platform не может быть null");
        Objects.requireNonNull(platformUserId, "platformUserId не может быть null");
        if (platformUserId.isBlank()) {
            throw new IllegalArgumentException("Идентификатор платформы не может быть пустым");
        }
    }
}
