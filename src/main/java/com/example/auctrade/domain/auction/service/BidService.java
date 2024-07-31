package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.BidDTO;

import java.util.List;

public interface BidService {

    Boolean updateBidPrice(BidDTO.Create request);
    Long findBidPriceByAuctionId(Long auctionId);
    String findBidUserByAuctionId(Long auctionId);
    void processBids(Long auctionId);
    List<BidDTO.List> getBidLogs(Long auctionId);
}
