package com.example.auctrade.domain.chat.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.auctrade.domain.chat.document.DirectChatMessage;
import com.example.auctrade.domain.chat.dto.DirectChatMessageDTO;
import com.example.auctrade.domain.chat.factory.ChatMessageFactory;
import com.example.auctrade.domain.chat.repository.DirectChatMessageRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectChatMessageServiceImpl implements DirectChatMessageService{
	private final DirectChatMessageRepository chatMessageRepository;
	private final ChatMessageFactory<DirectChatMessage> chatMessageFactory;
	@Override
	public DirectChatMessageDTO.Get saveChatMessage(DirectChatMessageDTO.Create requestDto) {
		DirectChatMessage chatMessage =
			chatMessageFactory.orderChatMessage(requestDto.getUsername(), requestDto.getMessage(), requestDto.getChatRoomId());
		return new DirectChatMessageDTO.Get(chatMessageRepository.save(chatMessage));
	}

	@Override
	public List<DirectChatMessageDTO.Get> findLog(Long directChatId) {
		return chatMessageRepository
			.findAllByDirectChatId(directChatId.toString())
			.stream()
			.map(DirectChatMessageDTO.Get::new)
			.toList();
	}
}
