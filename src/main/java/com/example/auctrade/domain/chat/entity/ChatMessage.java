package com.example.auctrade.domain.chat.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.redis.core.index.Indexed;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class ChatMessage {
    private String username;
    private String message;
    @Indexed // 날짜 로그 인덱싱 적용
    private String createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public ChatMessage(String username, String message) {
        this.username = username;
        this.message = message;
        this.createdAt = LocalDateTime.now().format(FORMATTER);
    }
}
