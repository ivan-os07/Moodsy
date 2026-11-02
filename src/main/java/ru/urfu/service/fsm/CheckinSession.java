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
     * Возвращает идентификатор пользователя — владельца диалога.
     *
     * @return идентификатор пользователя
     */
    public UUID getUserId() {
        return userId;
    }

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
     * Возвращает текущий шаг диалога
     *
     * @return текущий шаг
     */
    CheckinStep getStep() {
        return this.step;
    }

    /**
     * Переводит диалог на указанный шаг.
     *
     * @param step новый шаг диалога
     */
    public void setStep(CheckinStep step) {
        this.step = step;
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
     * Сохраняет оценку, введённую на шаге SCORE.
     *
     * @param score оценка от 1 до 10
     */
    public void setScore(int score) {
        this.score = score;
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
     * Сохраняет заметку, введённую на шаге NOTE.
     *
     * @param note текст заметки; пустая строка, если заметки нет
     */
    public void setNote(String note) {
        this.note = note;
    }

}
