package com.example.auctrade.chat.document;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Document(collection = "chatMessage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionChatMessage extends ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String auctionId;

    @Builder
    public AuctionChatMessage(String username, String message, Long auctionId){
        super(username, message);
        this.auctionId = auctionId.toString();
    }
}