package ru.urfu.service.fsm.checkin;

import ru.urfu.model.Sphere;

import java.util.UUID;

/**
 * Состояние одного чекина.<br>
 * Его цель единственна - хранить данные.<br>
 * Вся логика "перемещения" сессий лежит на CheckinFsmService.<br>
 * Record используется для хранения неизменяемого состояния диалога<br>
 * Пересоздаётся каждый раз при изменении шага, удаляется после сохранения записи или отмены.
 *
 * @param userId идентификатор пользователя, которому принадлежит диалог.
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
        String note) {
}
