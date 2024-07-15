package com.example.auctrade.chat.factory;

import com.example.auctrade.chat.document.AuctionChatMessage;
import org.springframework.stereotype.Component;

// 팩토리 메소드 컴포넌트 등록
@Component
public class AuctionChatMessageFactory implements ChatMessageFactory<AuctionChatMessage> {
    @Override
    public AuctionChatMessage createChatMessage(String username, String message, Long auctionId) {
        return new AuctionChatMessage(username, message, auctionId);
    }
}
