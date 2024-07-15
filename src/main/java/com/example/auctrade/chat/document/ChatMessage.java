package com.example.auctrade.chat.document;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Document(collection = "chatMessage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String username;
    private String message;
    private String auctionId;
    private String createdAt;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Builder
    public ChatMessage(String username, String message, String auctionId){
        this.username = username;
        this.message = message;
        this.auctionId = auctionId;
        this.createdAt = LocalDateTime.now().format(FORMATTER);
    }
}