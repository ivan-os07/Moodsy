package ru.urfu.service.fsm;

import java.util.UUID;

/**
 * Состояние одного диалога чекина: текущий шаг и данные, введённые пользователем.
 * Создаётся при запуске /checkin, удаляется после сохранения записи или отмены.
 */
public class CheckinSession {
    /**
     * Идентификатор пользователя, которому принадлежит диалог.
     */
    private final UUID userId;

    /**
     * Текущий шаг
     */
    private CheckinStep step;

    /**
     * Текущее значение оценки
     */
    private int score;

    /**
     * Текущее значение заметки
     */
    private String note;

    /**
     * Создаёт сессию на первом шаге диалога.
     *
     * @param userId идентификатор пользователя
     */
    public CheckinSession(UUID userId) {
        this.userId = userId;
        this.step = CheckinStep.SCORE;
        this.note = "";
    }

    /**
     * Возвращает идентификатор пользователя — владельца диалога.
     *
     * @return идентификатор пользователя
     */
    public UUID getUserId() {
        return userId;
    }

    /**
     * Возвращает текущий шаг диалога
     *
     * @return текущий шаг
     */
    public CheckinStep getStep() {
        return this.step;
    }

    /**
     * Возвращает введённую оценку настроения.
     *
     * @return оценка от 1 до 10 или 0, если оценка ещё не введена
     */
    public int getScore() {
        return score;
    }

    /**
     * Возвращает заметку пользователя.
     *
     * @return текст заметки; пустая строка означает, что заметки нет
     */
    public String getNote() {
        return note;
    }

    /**
     * Принимает оценку на шаге SCORE и переводит диалог на шаг NOTE.
     *
     * @param score оценка от 1 до 10
     * @throws IllegalArgumentException если оценка вне диапазона
     * @throws IllegalStateException    если диалог не находится на шаге SCORE
     */
    void applyScore(int score) {
        if (step != CheckinStep.SCORE) {
            throw new IllegalStateException("Оценка принимается только на шаге SCORE");
        }
        if (score < 1 || score > 10) {
            throw new IllegalArgumentException("Оценка должна быть от 1 до 10");
        }
        this.score = score;
        this.step = CheckinStep.NOTE;
    }

    /**
     * Принимает заметку на шаге NOTE и переводит диалог на шаг PREVIEW.
     *
     * @param note текст заметки; null и пустая строка означают отсутствие заметки
     * @throws IllegalStateException если диалог не находится на шаге NOTE
     */
    void applyNote(String note) {
        if (step != CheckinStep.NOTE) {
            throw new IllegalStateException("Заметка принимается только на шаге NOTE");
        }
        this.note = note == null ? "" : note.trim();
        this.step = CheckinStep.PREVIEW;
    }

    /**
     * Пропускает ввод заметки на шаге NOTE и переводит диалог на шаг PREVIEW.
     *
     * @throws IllegalStateException если диалог не находится на шаге NOTE
     */
    void skipNote() {
        if (step != CheckinStep.NOTE) {
            throw new IllegalStateException("Пропустить заметку можно только на шаге NOTE");
        }
        this.note = "";
        this.step = CheckinStep.PREVIEW;
    }

    /**
     * Возвращает диалог на предыдущий шаг.
     *
     * @return true, если шаг изменён; false, если диалог уже на первом шаге
     */
    boolean goBack() {
        return switch (step) {
            case PREVIEW -> {
                this.step = CheckinStep.NOTE;
                yield true;
            }
            case NOTE -> {
                this.step = CheckinStep.SCORE;
                yield true;
            }
            case SCORE -> false;
            default -> throw new IllegalStateException("Неизвестный шаг: " + step);
        };
    }

}
