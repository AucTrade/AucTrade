package com.example.auctrade.domain.chat.factory;

import org.springframework.stereotype.Component;

import com.example.auctrade.domain.chat.document.DirectChatMessage;
@Component
public class DirectChatMessageFactory implements ChatMessageFactory<DirectChatMessage>{
	@Override
	public DirectChatMessage createChatMessage(String username, String message, Long chatRoomId) {

		return new DirectChatMessage(username, message, chatRoomId);
	}
}
