package com.example.auctrade.domain.chat.service;

import java.util.List;

import com.example.auctrade.domain.chat.dto.DirectChatMessageDTO;

public interface DirectChatMessageService {
	DirectChatMessageDTO.Get saveChatMessage(DirectChatMessageDTO.Create requestDto);

	List<DirectChatMessageDTO.Get> findLog(Long directChatId);
}
