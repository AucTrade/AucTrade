package com.example.auctrade.domain.auction.service;

import com.example.auctrade.domain.auction.dto.BidDTO;

import java.util.List;

public interface BidService {

    Boolean updateBidPrice(BidDTO.Create request);

    Long findByAuctionId(Long auctionId);

    List<BidDTO.List> getBidLogs(Long auctionId);
}
