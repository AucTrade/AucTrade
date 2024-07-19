package com.example.auctrade.domain.chat.document;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.redis.core.index.Indexed;

@Getter
@Document(collection = "chatMessage")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuctionChatMessage extends ChatMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id; // 도큐먼트의 아이디
    private String auctionId; // 경매 아이디
    @Indexed
    private boolean isBid; // 입찰 메세지인지? 단순 의사표현 메세지인지?

    public AuctionChatMessage(String username, String message, Long auctionId, boolean isBid) {
        super(username, message);
        this.auctionId = auctionId.toString();
        this.isBid = isBid;
    }
}