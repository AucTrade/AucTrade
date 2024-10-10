package com.example.auctrade.domain.chat.service;

import com.example.auctrade.domain.chat.dto.AuctionMessageDTO;

import java.util.List;

public interface AuctionChatMessageService {

    AuctionMessageDTO.Get saveChatMessage(AuctionMessageDTO.Create requestDto);

    List<AuctionMessageDTO.Get> findLog(String auctionId);

    List<AuctionMessageDTO.Get> findAuctionLog(String auctionId);
}
