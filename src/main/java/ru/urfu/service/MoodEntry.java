package ru.urfu.service;

import java.time.LocalDateTime;

public record MoodEntry(LocalDateTime createdAt, int score) {

}
