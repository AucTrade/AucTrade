package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.BidDTO;

import java.util.List;

public interface BidService {

    BidDTO.Result updateBidPrice(BidDTO.Create request);
    BidDTO.Get getBid(Long auctionId);
    long getBidPrice(Long auctionId);
    String getBidUser(Long auctionId);
    void processBids(Long auctionId);
    List<BidDTO.List> getBidLogs(Long auctionId);
}
