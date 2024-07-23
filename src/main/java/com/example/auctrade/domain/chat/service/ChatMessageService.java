package com.example.auctrade.domain.chat.service;

import com.example.auctrade.domain.chat.dto.MessageDTO;

import java.util.List;

public interface ChatMessageService {

    MessageDTO.Get saveChatMessage(MessageDTO.Create requestDto);

    List<MessageDTO.Get> findLog(String auctionId);

    List<MessageDTO.Get> findAuctionLog(String auctionId);
}
