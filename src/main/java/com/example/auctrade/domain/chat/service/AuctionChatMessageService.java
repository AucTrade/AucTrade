package com.example.auctrade.domain.chat.service;

import com.example.auctrade.domain.chat.dto.AuctionMessageDto;

import java.util.List;

public interface AuctionChatMessageService {

    AuctionMessageDto.Get createEnterChatMessage(Long auctionId, String email);

    AuctionMessageDto.Get createChatMessage(AuctionMessageDto.Create requestDto, String email);

    List<AuctionMessageDto.Get> findLog(Long auctionId);
}
