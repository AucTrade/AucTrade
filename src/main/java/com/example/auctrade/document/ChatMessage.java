package com.example.auctrade.document;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chatMessage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String username;
    private String message;
    private String roomId;
    private String createdAt;

    @Builder
    public ChatMessage(String username, String message, String roomId){
        this.username = username;
        this.message = message;
        this.roomId = roomId;
    }
}