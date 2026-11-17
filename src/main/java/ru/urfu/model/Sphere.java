package ru.urfu.model;

/**
 * Сферы жизни, к которым пользователь может отнести свою отметку настроения
 */

public enum Sphere {
    /**
     * Работа и учеба
     */
    WORK("Дела"),
    /**
     * Взаимодействие с другими людьми
     */
    SOCIAL("Общение"),
    /**
     * Физическое и эмоциональное состояние
     */
    HEALTH("Здоровье"),
    /**
     * Отдых, сон, восстановление
     */
    REST("Отдых");
    /**
     * Название сферы для пользователя
     */
    private final String displayName;

    /**
     * Создает сферу с отображаемым названием
     *
     * @param displayName название сферы для пользователя
     */
    Sphere(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Возвращает локализованное название сферы
     *
     * @return отображаемое название сферы
     */
    public String getDisplayName() {
        return displayName;
    }
}
