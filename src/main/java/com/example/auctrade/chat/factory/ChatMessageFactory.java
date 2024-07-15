package com.example.auctrade.chat.factory;

import com.example.auctrade.chat.document.ChatMessage;

// 팩토리 메소드 패턴 최상위 인터페이스
public interface ChatMessageFactory<T extends ChatMessage> {

    default T orderChatMessage(String username, String message, Long auctionId) {
        validate(username, message, auctionId);

        return createChatMessage(username, message, auctionId);
    }

    T createChatMessage(String username, String message, Long auctionId);

    // 채팅 메세지 유효성 검사 메소드
    private void validate(String username, String message, Long auctionId) {
        //TODO: 유효성 로직 작성
    }
}
