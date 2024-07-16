package com.example.auctrade.domain.chat.document;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "chatMessage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionChatMessage extends ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    private String auctionId;

    public AuctionChatMessage(String username, String message, Long auctionId){
        super(username, message);
        this.auctionId = auctionId.toString();
    }
}