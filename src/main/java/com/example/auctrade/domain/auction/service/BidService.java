package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.BidDTO;

import java.util.List;

public interface BidService {

    BidDTO.Get getCurrentBid(Long auctionId);
    int getBidPrice(Long auctionId);
    String getBidUser(Long auctionId);
    BidDTO.Result placeBid(BidDTO.Create request);
    List<BidDTO.List> getBidLogs(Long auctionId);
    void removeMyBidLog(String email);
}
