package ru.urfu.application;

/**
 * Тип взаимодействия, определяющий вид клавиатуры,
 * которую бот показывает пользователю
 */
public enum InteractionType {
    /**
     * Кнопки не требуются
     */
    NONE,
    /**
     * Выбор оценки настроения
     * от 1 до 10
     */
    SCORE_SELECTION,
    /**
     * Выбор сферы
     */
    SPHERE_SELECTION,
    /**
     * Ввод или пропуск заметки
     */
    NOTE_INPUT,
    /**
     * Подтверждает или отменяет получившуюся заметку
     */
    PREVIEW_CONFIRMATION
}
