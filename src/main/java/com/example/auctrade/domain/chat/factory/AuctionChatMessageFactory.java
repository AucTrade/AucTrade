package com.example.auctrade.domain.chat.factory;

import com.example.auctrade.domain.chat.document.AuctionChatMessage;
import org.springframework.stereotype.Component;

// 팩토리 메소드 컴포넌트 등록
@Component
public class AuctionChatMessageFactory implements ChatMessageFactory<AuctionChatMessage> {
    @Override
    public AuctionChatMessage createChatMessage(String username, String message, Long auctionId) {
        // 입찰 메세지라면
        if (message.charAt(0) == '@'){
            return new AuctionChatMessage(username, message, auctionId, true);
        }

        return new AuctionChatMessage(username, message, auctionId, false);
    }
}
