/**
 * Сервис Обработки команд бота.
 * <p>
 * Содержит компоненты маршрутизации сообщений ({@link ru.urfu.commands.CommandDispatcher}),
 * интерфейсы и реализации команд бота ({@link ru.urfu.commands.BotCommand}),
 * валидацию пользовательского ввода и формирование платформо-независимых ответов ({@link ru.urfu.commands.BotResponse}).
 * </p>
 * Пакет полностью изолирован от внешних библиотек мессенджеров (Telegram API).
 */
package ru.urfu.commands;