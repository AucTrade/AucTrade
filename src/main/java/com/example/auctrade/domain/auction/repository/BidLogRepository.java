package com.example.auctrade.domain.auction.repository;

import com.example.auctrade.domain.auction.document.BidLog;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface BidLogRepository  extends MongoRepository<BidLog, String> {
    List<BidLog> findAllByAuctionId(Long auctionId);
}
