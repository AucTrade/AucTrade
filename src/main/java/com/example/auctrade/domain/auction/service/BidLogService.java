package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.AuctionDTO;
import com.example.auctrade.domain.chat.dto.MessageDTO;
import com.example.auctrade.domain.chat.service.ChatMessageService;

import java.util.List;
import java.util.Map;

public interface BidLogService {

    AuctionDTO.BidResult updateBidPrice(AuctionDTO.Bid requestDto);

    List<AuctionDTO.BidList> getBidLogs(String auctionId);
}
