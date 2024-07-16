package com.example.auctrade.domain.chat.document;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ChatMessage {
    private String username;
    private String message;
    private String createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
        this.createdAt = LocalDateTime.now().format(FORMATTER);
    }
}
