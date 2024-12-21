package com.example.auctrade.domain.chat.dto;

import com.example.auctrade.domain.chat.entity.DirectChatMessage;

import lombok.*;

public class DirectChatMessageDTO {

	@Getter
	@Setter
	@AllArgsConstructor
	public static class Create {
		private Long chatRoomId; // 1:1 채팅방 ID
		private String username; // 메시지를 보낸 사용자
		private String message;  // 메시지 내용
	}

	@Getter
	@Setter
	public static class Get {
		private final Long chatRoomId;
		private final String username; // 메시지를 보낸 사용자
		private final String message;
		private final String createAt;

		public Get(DirectChatMessage chatMessage) {
			this.chatRoomId = Long.parseLong(chatMessage.getDirectChatId());
			this.username = chatMessage.getEmail(); // 메시지를 보낸 사람
			this.message = chatMessage.getMessage();
			this.createAt = chatMessage.getCreatedAt().toString();
		}
	}
}
