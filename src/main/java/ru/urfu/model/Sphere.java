package ru.urfu.model;

public enum Sphere {
    WORK("Дела"),
    SOCIAL("Общение"),
    HEALTH("Здоровье"),
    REST("Отдых");

    /**
     * Имя сферы
     */
    private final String displayName;

    /**
     * Конструктор
     * @param displayName имя
     */
    Sphere(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Получение имени
     * @return displayName
     */
    public String getDisplayName() {
        return displayName;
    }
}
