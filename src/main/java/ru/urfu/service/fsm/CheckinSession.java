package ru.urfu.service.fsm;

import java.util.UUID;

/**
 * Состояние одного чекина.<br>
 * Его цель единственна - хранить данные.<br>
 * Вся логика "перемещения" сессий лежит на CheckinFsmService.<br>
 * Намеренный record обеспечивает потокобезопасность.<br>
 * Пересоздаётся каждый раз при изменении шага, удаляется после сохранения записи или отмены.
 *
 * @param userId идентификатор пользователя, которому принадлежит диалог.
 * @param step   текущий шаг
 * @param score  текущее значение оценки
 * @param note   текущее значение заметки
 */
public record CheckinSession(UUID userId, CheckinStep step, int score, String note) {

}
