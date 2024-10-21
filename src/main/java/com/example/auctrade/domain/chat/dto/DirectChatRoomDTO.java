package com.example.auctrade.domain.chat.dto;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.springframework.web.socket.WebSocketSession;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DirectChatRoomDTO implements Serializable {
	private Long id;	// chattingRoomID
	private String seller;
	private String buyer;
	private String createdTime;
	private Set<WebSocketSession> sessions = new HashSet<>();

	@Builder
	public DirectChatRoomDTO(Long id, String seller, String buyer){
		this.id = id;
		this.seller = seller;
		this.buyer = buyer;
	}
	public void addSession(WebSocketSession session) {
		sessions.add(session);
	}

	public void removeSession(WebSocketSession session) {
		sessions.remove(session);
	}
}
