package ru.urfu.service.fsm.checkin;

import ru.urfu.model.Sphere;
import ru.urfu.service.fsm.UserSession;

import java.util.Objects;
import java.util.UUID;

/**
 * Состояние одного чекина.<br>
 * Реализует интерфейс {@link UserSession}. <br>
 * Его цель единственна - хранить данные.<br>
 * Вся логика "перемещения" сессий лежит на CheckinFsmService.<br>
 * Record используется для хранения неизменяемого состояния диалога<br>
 * Пересоздаётся каждый раз при изменении шага, удаляется после сохранения записи или отмены.
 *
 * @param step   текущий шаг
 * @param score  текущее значение оценки
 * @param sphere выбранная сфера; null, пока пользователь не прошёл шаг SPHERE
 * @param note   текущее значение заметки
 */
public record CheckinSession(
        UUID userId,
        CheckinStep step,
        int score,
        Sphere sphere,
        String note
) implements UserSession {

    /**
     * Проверяет обязательные поля и нормализует заметку.
     *
     * @throws NullPointerException если {@code userId} или {@code step} равны {@code null}
     */
    public CheckinSession {
        Objects.requireNonNull(userId, "userId не может быть null");
        Objects.requireNonNull(step, "step не может быть null");
        note = note == null ? "" : note;
    }

    /**
     * Создаёт новую сессию на начальном шаге CheckinStep SCORE.
     *
     * @param userId идентификатор пользователя
     */
    public CheckinSession(UUID userId) {
        this(userId, CheckinStep.SCORE, 0, null, "");
    }

    /**
     * Возвращает копию сессии с новым шагом (для навигации назад).
     *
     * @param newStep новый шаг диалога
     * @return обновлённая сессия
     */
    public CheckinSession withStep(CheckinStep newStep) {
        return new CheckinSession(userId, newStep, score, sphere, note);
    }

    /**
     * Сохраняет оценку и переводит сессию на указанный шаг.
     *
     * @param newScore новая оценка настроения
     * @param nextStep следующий шаг диалога
     * @return обновлённая сессия
     */
    public CheckinSession withScore(int newScore, CheckinStep nextStep) {
        return new CheckinSession(userId, nextStep, newScore, sphere, note);
    }

    /**
     * Сохраняет сферу и переводит сессию на указанный шаг.
     *
     * @param newSphere выбранная сфера
     * @param nextStep  следующий шаг диалога
     * @return обновлённая сессия
     */
    public CheckinSession withSphere(Sphere newSphere, CheckinStep nextStep) {
        return new CheckinSession(userId, nextStep, score, newSphere, note);
    }

    /**
     * Сохраняет заметку и переводит сессию на указанный шаг.
     *
     * @param newNote  текст заметки
     * @param nextStep следующий шаг диалога
     * @return обновлённая сессия
     */
    public CheckinSession withNote(String newNote, CheckinStep nextStep) {
        return new CheckinSession(userId, nextStep, score, sphere, newNote);
    }
}