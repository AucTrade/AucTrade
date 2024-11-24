package com.example.auctrade.domain.chat.factory;

import com.example.auctrade.domain.chat.entity.ChatMessage;

// 팩토리 메소드 패턴 최상위 인터페이스
public interface ChatMessageFactory<T extends ChatMessage> {

    default T orderChatMessage(String username, String message, Long id) {
        validate(username, message, id);

        return createChatMessage(username, message, id);
    }

    T createChatMessage(String username, String message, Long id);

    // 채팅 메세지 유효성 검사 메소드
    private void validate(String username, String message, Long id) {
        //TODO: 유효성 로직 작성
    }
}
