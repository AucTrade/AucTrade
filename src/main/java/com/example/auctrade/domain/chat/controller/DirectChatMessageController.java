package com.example.auctrade.domain.chat.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.auctrade.domain.chat.dto.DirectChatMessageDTO;
import com.example.auctrade.domain.chat.service.DirectChatMessageServiceImpl;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@RestController
@RequestMapping("/api/chat/direct")
@RequiredArgsConstructor
@Log4j2
public class DirectChatMessageController {

	private final SimpMessageSendingOperations sendingOperations;
	private final DirectChatMessageServiceImpl chatMessageService;

	// 사용자가 1:1 채팅방에 입장할 때
	@MessageMapping(value = "/enter")
	public void enter(DirectChatMessageDTO.Create message, Principal principal) {
		log.info("{} 1:1 채팅방 입장", principal.getName());
		message.setUsername(principal.getName());
		message.setMessage(principal.getName() + " 님이 채팅방에 입장하였습니다.");

		// 채팅 메시지 저장 후 모든 사용자에게 알림
		DirectChatMessageDTO.Get responseDto = chatMessageService.saveChatMessage(message);
		if (responseDto != null) {
			sendingOperations.convertAndSend("/sub/chat/room/" + message.getChatRoomId(), responseDto);
		}
	}

	// 사용자가 메시지를 보낼 때
	@MessageMapping(value = "/message")
	public void sendMessage(DirectChatMessageDTO.Create message, Principal principal) {
		log.info("{} 메시지 전송: {}", principal.getName(), message.getMessage());
		message.setUsername(principal.getName());

		// 채팅 메시지 저장 후 모든 사용자에게 알림
		DirectChatMessageDTO.Get responseDto = chatMessageService.saveChatMessage(message);
		if (responseDto != null) {
			sendingOperations.convertAndSend("/sub/chat/room/" + message.getChatRoomId(), responseDto);
		}
	}

	// 특정 채팅방의 채팅 로그 조회
	@GetMapping("/{directChatId}")
	public ResponseEntity<List<DirectChatMessageDTO.Get>> getChatLogs(@PathVariable Long directChatId) {
		log.info("채팅방 {} 의 채팅 로그 조회", directChatId);
		List<DirectChatMessageDTO.Get> chatlog = chatMessageService.findLog(directChatId);
		return ResponseEntity.ok(chatlog);
	}
}