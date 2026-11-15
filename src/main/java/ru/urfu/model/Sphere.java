package ru.urfu.model;

public enum Sphere {
    WORK("Дела"),
    SOCIAL("Общение"),
    HEALTH("Здоровье"),
    REST("Отдых");

    private final String displayName;
    Sphere(String displayName) {
        this.displayName = displayName;
    }
    public String getDisplayName() {
        return displayName;
    }
}
